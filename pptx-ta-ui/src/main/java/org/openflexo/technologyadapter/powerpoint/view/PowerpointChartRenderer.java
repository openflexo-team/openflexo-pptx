/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Openflexo-technology-adapters-ui, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.technologyadapter.powerpoint.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.xddf.usermodel.chart.RadarStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDoughnutChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFRadarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointChart;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointChartSeries;

/**
 * Draws a {@link PowerpointChart} with Java2D, from the series of the model, since POI does not render charts.<br>
 * Covers column, bar, line (and their combinations, one value scale per kind of chart), pie, doughnut and radar charts. The rendering is
 * a simplified one: series colors, legend, axes and value labels follow the chart, but not its fonts, number formats or effects.
 * 
 * @author sylvain
 * 
 */
public class PowerpointChartRenderer {

	/**
	 * Colors of the default Office theme, used for a series or a point without explicit color
	 */
	private static final Color[] PALETTE = { new Color(0x4472C4), new Color(0xED7D31), new Color(0xA5A5A5), new Color(0xFFC000),
			new Color(0x5B9BD5), new Color(0x70AD47), new Color(0x264478), new Color(0x9E480E) };

	private static final Color DEFAULT_TEXT_COLOR = new Color(0x40, 0x40, 0x40);
	private static final Color AXIS_COLOR = new Color(0xA0, 0xA0, 0xA0);
	private static final Color GRID_COLOR = new Color(0xE0, 0xE0, 0xE0);

	private final PowerpointChart chart;
	private final Graphics2D g;
	private final Font font;
	private final Font titleFont;
	private final Color textColor;
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);

	/**
	 * Draw the chart in the supplied area
	 * 
	 * @param graphics
	 *            the graphics, in the coordinates of the area
	 * @param chart
	 *            the chart to draw
	 * @param area
	 *            the area of the chart, in points
	 */
	public static void paint(Graphics2D graphics, PowerpointChart chart, Rectangle2D area) {
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g.clip(area);
			new PowerpointChartRenderer(chart, g, area).paint(area);
		} finally {
			g.dispose();
		}
	}

	private PowerpointChartRenderer(PowerpointChart chart, Graphics2D g, Rectangle2D area) {
		this.chart = chart;
		this.g = g;
		float size = (float) Math.max(5, Math.min(11, Math.min(area.getWidth(), area.getHeight()) / 22));
		font = new Font(Font.SANS_SERIF, Font.PLAIN, 12).deriveFont(size);
		titleFont = font.deriveFont(Font.BOLD, size * 1.25f);
		textColor = chart.getTextColor() != null ? chart.getTextColor() : DEFAULT_TEXT_COLOR;
		numberFormat.setMaximumFractionDigits(1);
	}

	private void paint(Rectangle2D area) {
		double pad = Math.min(area.getWidth(), area.getHeight()) * 0.04;
		Rectangle2D inner = new Rectangle2D.Double(area.getX() + pad, area.getY() + pad, area.getWidth() - 2 * pad,
				area.getHeight() - 2 * pad);
		if (chart.getTitle() != null) {
			g.setFont(titleFont);
			g.setColor(textColor);
			double h = g.getFontMetrics().getHeight();
			drawText(chart.getTitle(), inner.getCenterX(), inner.getY() + g.getFontMetrics().getAscent(), 0);
			inner = new Rectangle2D.Double(inner.getX(), inner.getY() + h * 1.3, inner.getWidth(), inner.getHeight() - h * 1.3);
		}
		List<PowerpointChartSeries> series = chart.getSeries();
		if (series.isEmpty()) {
			return;
		}
		boolean circular = isCircular(series.get(0));
		inner = paintLegend(inner, series, circular);
		g.setFont(font);
		String type = series.get(0).getChartType();
		if (circular) {
			paintPie(inner, series.get(0));
		}
		else if ("radar".equals(type)) {
			paintRadar(inner, series);
		}
		else {
			paintCartesian(inner, series);
		}
	}

	private static boolean isCircular(PowerpointChartSeries series) {
		return "pie".equals(series.getChartType()) || "doughnut".equals(series.getChartType());
	}

	// ---- Legend

	/**
	 * Paint the legend at its position, and return the area left for the plot
	 */
	private Rectangle2D paintLegend(Rectangle2D area, List<PowerpointChartSeries> series, boolean circular) {
		String position = chart.getLegendPosition();
		if (position == null) {
			return area;
		}
		List<String> labels = new ArrayList<>();
		List<Color> colors = new ArrayList<>();
		List<Boolean> lines = new ArrayList<>();
		if (circular) {
			PowerpointChartSeries first = series.get(0);
			List<String> categories = first.getCategories();
			for (int i = 0; i < categories.size(); i++) {
				labels.add(categories.get(i));
				colors.add(pointColor(first, i));
				lines.add(false);
			}
		}
		else {
			for (int i = 0; i < series.size(); i++) {
				labels.add(series.get(i).getName());
				colors.add(seriesColor(series.get(i), i));
				boolean line = "line".equals(series.get(i).getChartType()) || "radar".equals(series.get(i).getChartType());
				lines.add(line);
				if (line) {
					colors.set(colors.size() - 1, lineColor(series.get(i), i));
				}
			}
		}
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		double swatch = metrics.getAscent() * 0.8;
		double lineHeight = metrics.getHeight() * 1.2;
		boolean vertical = position.equals("left") || position.equals("right") || position.equals("top right");
		if (vertical) {
			double width = 0;
			for (String label : labels) {
				width = Math.max(width, swatch * 1.6 + metrics.stringWidth(label));
			}
			width = Math.min(width, area.getWidth() * 0.4);
			double x = position.equals("left") ? area.getX() : area.getMaxX() - width;
			double y = area.getCenterY() - lineHeight * labels.size() / 2;
			for (int i = 0; i < labels.size(); i++) {
				paintLegendEntry(labels.get(i), colors.get(i), lines.get(i), x, y + i * lineHeight, swatch);
			}
			double gap = swatch;
			return position.equals("left") ? new Rectangle2D.Double(area.getX() + width + gap, area.getY(), area.getWidth() - width - gap,
					area.getHeight()) : new Rectangle2D.Double(area.getX(), area.getY(), area.getWidth() - width - gap, area.getHeight());
		}
		// Entries are laid out in rows, as many as needed for the width of the chart
		List<List<Integer>> rows = new ArrayList<>();
		List<Double> rowWidths = new ArrayList<>();
		double rowWidth = 0;
		for (int i = 0; i < labels.size(); i++) {
			double entry = swatch * 1.6 + metrics.stringWidth(labels.get(i) != null ? labels.get(i) : "") + swatch * 1.5;
			if (rows.isEmpty() || (rowWidth + entry > area.getWidth() && !rows.get(rows.size() - 1).isEmpty())) {
				if (!rows.isEmpty()) {
					rowWidths.add(rowWidth);
				}
				rows.add(new ArrayList<>());
				rowWidth = 0;
			}
			rows.get(rows.size() - 1).add(i);
			rowWidth += entry;
		}
		rowWidths.add(rowWidth);
		double height = lineHeight * rows.size();
		double y = position.equals("top") ? area.getY() : area.getMaxY() - height;
		for (int r = 0; r < rows.size(); r++) {
			double x = area.getCenterX() - Math.min(rowWidths.get(r), area.getWidth()) / 2;
			for (int i : rows.get(r)) {
				paintLegendEntry(labels.get(i), colors.get(i), lines.get(i), x, y + r * lineHeight, swatch);
				x += swatch * 1.6 + metrics.stringWidth(labels.get(i) != null ? labels.get(i) : "") + swatch * 1.5;
			}
		}
		double used = height + lineHeight * 0.2;
		return position.equals("top") ? new Rectangle2D.Double(area.getX(), area.getY() + used, area.getWidth(), area.getHeight() - used)
				: new Rectangle2D.Double(area.getX(), area.getY(), area.getWidth(), area.getHeight() - used);
	}

	private void paintLegendEntry(String label, Color color, boolean line, double x, double y, double swatch) {
		FontMetrics metrics = g.getFontMetrics();
		double middle = y + metrics.getHeight() / 2;
		g.setColor(color);
		if (line) {
			g.setStroke(new BasicStroke((float) Math.max(1, swatch / 4)));
			g.draw(new Line2D.Double(x, middle, x + swatch * 1.2, middle));
		}
		else {
			g.fill(new Rectangle2D.Double(x, middle - swatch / 2, swatch, swatch));
		}
		g.setColor(textColor);
		g.drawString(label != null ? label : "", (float) (x + swatch * 1.6), (float) (middle + metrics.getAscent() / 2.5));
	}

	// ---- Column, bar and line charts

	/**
	 * A kind of chart of the plot area (one POI chart data), with its series and its value scale
	 */
	private class Group {
		final XDDFChartData data;
		final List<PowerpointChartSeries> series = new ArrayList<>();
		final List<Integer> indexes = new ArrayList<>();
		double min;
		double max;
		double step;

		Group(XDDFChartData data) {
			this.data = data;
		}

		String type() {
			return series.get(0).getChartType();
		}

		boolean isAxisVisible() {
			List<XDDFValueAxis> axes = data != null ? data.getValueAxes() : null;
			return axes == null || axes.isEmpty() || axes.get(0).isVisible();
		}

		long axisId() {
			List<XDDFValueAxis> axes = data != null ? data.getValueAxes() : null;
			return axes != null && !axes.isEmpty() ? axes.get(0).getId() : -1;
		}

		void shareScaleWith(Group other) {
			double low = Math.min(min, other.min);
			double high = Math.max(max, other.max);
			min = low;
			max = high;
			other.min = low;
			other.max = high;
			step = other.step = niceStep((high - low) / 5);
		}

		void computeScale() {
			double low = 0;
			double high = 0;
			for (PowerpointChartSeries s : series) {
				for (Double value : s.getValues()) {
					if (value != null) {
						low = Math.min(low, value);
						high = Math.max(high, value);
					}
				}
			}
			if (high == low) {
				high = low + 1;
			}
			step = niceStep((high - low) / 5);
			min = Math.floor(low / step) * step;
			max = Math.ceil(high / step) * step;
			List<XDDFValueAxis> axes = data != null ? data.getValueAxes() : null;
			if (axes != null && !axes.isEmpty() && (axes.get(0).isSetMinimum() || axes.get(0).isSetMaximum())) {
				if (axes.get(0).isSetMinimum()) {
					min = axes.get(0).getMinimum();
				}
				if (axes.get(0).isSetMaximum()) {
					max = axes.get(0).getMaximum();
				}
				if (max <= min) {
					max = min + step;
				}
				// The graduation follows the bounds given by the axis
				step = niceStep((max - min) / 5);
			}
		}
	}

	private static double niceStep(double raw) {
		double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
		double fraction = raw / magnitude;
		double nice = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 2.5 ? 2.5 : fraction <= 5 ? 5 : 10;
		return nice * magnitude;
	}

	private void paintCartesian(Rectangle2D area, List<PowerpointChartSeries> allSeries) {
		Map<XDDFChartData, Group> groups = new LinkedHashMap<>();
		for (int i = 0; i < allSeries.size(); i++) {
			PowerpointChartSeries s = allSeries.get(i);
			Group group = groups.computeIfAbsent(s.getChartData(), Group::new);
			group.series.add(s);
			group.indexes.add(i);
		}
		List<Group> list = new ArrayList<>(groups.values());
		for (Group group : list) {
			group.computeScale();
		}
		boolean horizontal = "bar".equals(list.get(0).type());
		List<String> categories = allSeries.get(0).getCategories();
		int count = Math.max(categories.size(), allSeries.get(0).getValues().size());
		if (count == 0) {
			return;
		}
		FontMetrics metrics = g.getFontMetrics();
		// The first kind of chart is read on the left axis, a second one on the right axis, unless both share the same axis
		Group left = list.get(0);
		Group right = list.size() > 1 ? list.get(1) : null;
		if (right != null && left.axisId() == right.axisId()) {
			left.shareScaleWith(right);
			right = null;
		}
		double leftWidth = left.isAxisVisible() ? scaleLabelWidth(left, metrics) + metrics.getAscent() * 0.6 : metrics.getAscent() * 0.3;
		double rightWidth = right != null && right.isAxisVisible() ? scaleLabelWidth(right, metrics) + metrics.getAscent() * 0.6
				: metrics.getAscent() * 0.3;
		double categoryHeight = metrics.getHeight() * 1.4;

		if (horizontal) {
			// Categories on the vertical axis, values along the horizontal one
			double labelWidth = 0;
			for (String category : categories) {
				labelWidth = Math.max(labelWidth, metrics.stringWidth(category != null ? category : ""));
			}
			labelWidth = Math.min(labelWidth + metrics.getAscent(), area.getWidth() * 0.35);
			Rectangle2D plot = new Rectangle2D.Double(area.getX() + labelWidth, area.getY(), area.getWidth() - labelWidth,
					area.getHeight() - (left.isAxisVisible() ? categoryHeight : 0));
			paintHorizontalBars(plot, left, categories, count, metrics);
			return;
		}

		Rectangle2D plot = new Rectangle2D.Double(area.getX() + leftWidth, area.getY() + metrics.getHeight() / 2,
				area.getWidth() - leftWidth - rightWidth, area.getHeight() - categoryHeight - metrics.getHeight() / 2);
		if (plot.getWidth() <= 0 || plot.getHeight() <= 0) {
			return;
		}
		if (chart.getPlotAreaColor() != null) {
			g.setColor(chart.getPlotAreaColor());
			g.fill(plot);
		}
		// Grid and value axes
		g.setStroke(new BasicStroke(0.5f));
		for (double v = left.min; v <= left.max + left.step / 1000; v += left.step) {
			double y = valueY(plot, left, v);
			g.setColor(GRID_COLOR);
			g.draw(new Line2D.Double(plot.getX(), y, plot.getMaxX(), y));
			if (left.isAxisVisible()) {
				g.setColor(textColor);
				String label = numberFormat.format(v);
				g.drawString(label, (float) (plot.getX() - metrics.getAscent() * 0.4 - metrics.stringWidth(label)),
						(float) (y + metrics.getAscent() / 2.5));
			}
		}
		if (right != null && right.isAxisVisible()) {
			g.setColor(textColor);
			for (double v = right.min; v <= right.max + right.step / 1000; v += right.step) {
				g.drawString(numberFormat.format(v), (float) (plot.getMaxX() + metrics.getAscent() * 0.4),
						(float) (valueY(plot, right, v) + metrics.getAscent() / 2.5));
			}
		}
		g.setColor(AXIS_COLOR);
		g.draw(new Line2D.Double(plot.getX(), valueY(plot, left, Math.max(left.min, 0)), plot.getMaxX(),
				valueY(plot, left, Math.max(left.min, 0))));

		// Category labels, skipping some when they do not fit
		double slot = plot.getWidth() / count;
		int every = 1;
		double widest = 0;
		for (String category : categories) {
			widest = Math.max(widest, metrics.stringWidth(category != null ? category : ""));
		}
		while (widest * 1.1 > slot * every && every < count) {
			every++;
		}
		g.setColor(textColor);
		for (int i = 0; i < categories.size(); i += every) {
			drawText(categories.get(i), plot.getX() + slot * (i + 0.5), plot.getMaxY() + metrics.getAscent() * 1.3, plot.getWidth());
		}

		// Columns first, then lines on top
		int columnSeries = 0;
		for (Group group : list) {
			if (!"line".equals(group.type())) {
				columnSeries += group.series.size();
			}
		}
		int column = 0;
		for (Group group : list) {
			if ("line".equals(group.type())) {
				continue;
			}
			for (int k = 0; k < group.series.size(); k++) {
				PowerpointChartSeries s = group.series.get(k);
				Color color = seriesColor(s, group.indexes.get(k));
				List<Double> values = s.getValues();
				double groupWidth = slot * 0.6;
				double barWidth = groupWidth / Math.max(1, columnSeries);
				for (int i = 0; i < values.size(); i++) {
					if (values.get(i) == null) {
						continue;
					}
					double x = plot.getX() + slot * i + (slot - groupWidth) / 2 + barWidth * column;
					double y0 = valueY(plot, group, Math.max(group.min, 0));
					double y1 = valueY(plot, group, values.get(i));
					g.setColor(pointColorOr(s, i, color));
					g.fill(new Rectangle2D.Double(x, Math.min(y0, y1), barWidth, Math.abs(y1 - y0)));
					if (s.isShowValues()) {
						g.setColor(textColor);
						drawText(numberFormat.format(values.get(i)), x + barWidth / 2, Math.min(y0, y1) - metrics.getDescent(), 0);
					}
				}
				column++;
			}
		}
		for (Group group : list) {
			if (!"line".equals(group.type())) {
				continue;
			}
			for (int k = 0; k < group.series.size(); k++) {
				PowerpointChartSeries s = group.series.get(k);
				paintLine(plot, group, s, lineColor(s, group.indexes.get(k)), slot, metrics);
			}
		}
	}

	private void paintLine(Rectangle2D plot, Group group, PowerpointChartSeries s, Color color, double slot, FontMetrics metrics) {
		List<Double> values = s.getValues();
		Path2D path = new Path2D.Double();
		boolean started = false;
		double marker = Math.max(2, metrics.getAscent() * 0.45);
		g.setColor(color);
		g.setStroke(new BasicStroke((float) Math.max(1.2, metrics.getAscent() * 0.2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) == null) {
				started = false;
				continue;
			}
			double x = plot.getX() + slot * (i + 0.5);
			double y = valueY(plot, group, values.get(i));
			if (started) {
				path.lineTo(x, y);
			}
			else {
				path.moveTo(x, y);
				started = true;
			}
		}
		g.draw(path);
		boolean markers = values.size() <= 12;
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) == null) {
				continue;
			}
			double x = plot.getX() + slot * (i + 0.5);
			double y = valueY(plot, group, values.get(i));
			if (markers) {
				g.setColor(color);
				g.fill(new Ellipse2D.Double(x - marker / 2, y - marker / 2, marker, marker));
			}
			if (s.isShowValues()) {
				g.setColor(textColor);
				drawText(numberFormat.format(values.get(i)), x, y - marker, 0);
			}
		}
	}

	private void paintHorizontalBars(Rectangle2D plot, Group group, List<String> categories, int count, FontMetrics metrics) {
		double slot = plot.getHeight() / count;
		g.setStroke(new BasicStroke(0.5f));
		for (double v = group.min; v <= group.max + group.step / 1000; v += group.step) {
			double x = plot.getX() + (v - group.min) / (group.max - group.min) * plot.getWidth();
			g.setColor(GRID_COLOR);
			g.draw(new Line2D.Double(x, plot.getY(), x, plot.getMaxY()));
			if (group.isAxisVisible()) {
				g.setColor(textColor);
				drawText(numberFormat.format(v), x, plot.getMaxY() + metrics.getAscent() * 1.3, 0);
			}
		}
		int seriesCount = group.series.size();
		for (int k = 0; k < seriesCount; k++) {
			PowerpointChartSeries s = group.series.get(k);
			Color color = seriesColor(s, group.indexes.get(k));
			List<Double> values = s.getValues();
			double groupHeight = slot * 0.6;
			double barHeight = groupHeight / seriesCount;
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) == null) {
					continue;
				}
				// The first category is drawn at the bottom, as PowerPoint does
				double y = plot.getMaxY() - slot * (i + 1) + (slot - groupHeight) / 2 + barHeight * k;
				double x0 = plot.getX() + (Math.max(group.min, 0) - group.min) / (group.max - group.min) * plot.getWidth();
				double x1 = plot.getX() + (values.get(i) - group.min) / (group.max - group.min) * plot.getWidth();
				g.setColor(pointColorOr(s, i, color));
				g.fill(new Rectangle2D.Double(Math.min(x0, x1), y, Math.abs(x1 - x0), barHeight));
				if (s.isShowValues()) {
					g.setColor(textColor);
					g.drawString(numberFormat.format(values.get(i)), (float) (Math.max(x0, x1) + metrics.getAscent() * 0.3),
							(float) (y + barHeight / 2 + metrics.getAscent() / 2.5));
				}
			}
		}
		g.setColor(textColor);
		for (int i = 0; i < categories.size(); i++) {
			String label = categories.get(i) != null ? categories.get(i) : "";
			g.drawString(label, (float) (plot.getX() - metrics.getAscent() * 0.5 - metrics.stringWidth(label)),
					(float) (plot.getMaxY() - slot * (i + 0.5) + metrics.getAscent() / 2.5));
		}
		g.setColor(AXIS_COLOR);
		g.draw(new Line2D.Double(plot.getX(), plot.getY(), plot.getX(), plot.getMaxY()));
	}

	private static double valueY(Rectangle2D plot, Group group, double value) {
		return plot.getMaxY() - (value - group.min) / (group.max - group.min) * plot.getHeight();
	}

	private double scaleLabelWidth(Group group, FontMetrics metrics) {
		double width = 0;
		for (double v = group.min; v <= group.max + group.step / 1000; v += group.step) {
			width = Math.max(width, metrics.stringWidth(numberFormat.format(v)));
		}
		return width;
	}

	// ---- Pie and doughnut charts

	private void paintPie(Rectangle2D area, PowerpointChartSeries s) {
		List<Double> values = s.getValues();
		double total = 0;
		for (Double value : values) {
			total += value != null && value > 0 ? value : 0;
		}
		if (total <= 0) {
			return;
		}
		double diameter = Math.min(area.getWidth(), area.getHeight());
		Rectangle2D circle = new Rectangle2D.Double(area.getCenterX() - diameter / 2, area.getCenterY() - diameter / 2, diameter, diameter);
		double hole = 0;
		if (s.getChartData() instanceof XDDFDoughnutChartData) {
			Integer holeSize = ((XDDFDoughnutChartData) s.getChartData()).getHoleSize();
			hole = (holeSize != null ? holeSize : 50) / 100.0;
		}
		Shape holeShape = new Ellipse2D.Double(circle.getCenterX() - diameter * hole / 2, circle.getCenterY() - diameter * hole / 2,
				diameter * hole, diameter * hole);
		// Slices are drawn clockwise from twelve o'clock
		double start = 90;
		for (int i = 0; i < values.size(); i++) {
			double value = values.get(i) != null && values.get(i) > 0 ? values.get(i) : 0;
			double extent = -value / total * 360;
			Area slice = new Area(new Arc2D.Double(circle, start, extent, Arc2D.PIE));
			if (hole > 0) {
				slice.subtract(new Area(holeShape));
			}
			g.setColor(pointColor(s, i));
			g.fill(slice);
			g.setColor(Color.WHITE);
			g.setStroke(new BasicStroke((float) Math.max(0.5, diameter / 200)));
			g.draw(slice);
			if (s.isShowValues() && value > 0) {
				double angle = Math.toRadians(start + extent / 2);
				double radius = diameter / 2 * (hole > 0 ? (1 + hole) / 2 : 0.65);
				g.setColor(Color.WHITE);
				drawText(numberFormat.format(value), circle.getCenterX() + Math.cos(angle) * radius,
						circle.getCenterY() - Math.sin(angle) * radius + g.getFontMetrics().getAscent() / 2.5, 0);
			}
			start += extent;
		}
	}

	// ---- Radar charts

	private void paintRadar(Rectangle2D area, List<PowerpointChartSeries> series) {
		List<String> categories = series.get(0).getCategories();
		int count = Math.max(categories.size(), series.get(0).getValues().size());
		if (count < 3) {
			return;
		}
		Group scale = new Group(series.get(0).getChartData());
		scale.series.addAll(series);
		scale.computeScale();
		scale.min = Math.min(0, scale.min);
		FontMetrics metrics = g.getFontMetrics();
		double labelRoom = metrics.getHeight() * 1.5;
		double widest = 0;
		for (String category : categories) {
			widest = Math.max(widest, metrics.stringWidth(category != null ? category : ""));
		}
		double radius = Math.max(1, Math.min(area.getWidth() / 2 - Math.min(widest, area.getWidth() / 4) - metrics.getAscent(),
				area.getHeight() / 2 - labelRoom));
		double cx = area.getCenterX();
		double cy = area.getCenterY();
		// Rings and spokes
		g.setStroke(new BasicStroke(0.5f));
		for (double v = scale.min + scale.step; v <= scale.max + scale.step / 1000; v += scale.step) {
			double r = (v - scale.min) / (scale.max - scale.min) * radius;
			g.setColor(GRID_COLOR);
			g.draw(polygon(cx, cy, r, count, null, scale));
			if (scale.isAxisVisible()) {
				g.setColor(textColor);
				g.drawString(numberFormat.format(v), (float) (cx + metrics.getAscent() * 0.2), (float) (cy - r));
			}
		}
		for (int i = 0; i < count; i++) {
			double angle = Math.toRadians(90 - 360.0 * i / count);
			g.setColor(GRID_COLOR);
			g.draw(new Line2D.Double(cx, cy, cx + Math.cos(angle) * radius, cy - Math.sin(angle) * radius));
			if (i < categories.size()) {
				g.setColor(textColor);
				double lx = cx + Math.cos(angle) * (radius + metrics.getAscent() * 0.6);
				double ly = cy - Math.sin(angle) * (radius + metrics.getAscent() * 0.6);
				String label = categories.get(i) != null ? categories.get(i) : "";
				double width = metrics.stringWidth(label);
				double x = Math.cos(angle) > 0.2 ? lx : Math.cos(angle) < -0.2 ? lx - width : lx - width / 2;
				double y = Math.sin(angle) > 0.2 ? ly : Math.sin(angle) < -0.2 ? ly + metrics.getAscent() : ly + metrics.getAscent() / 2.5;
				g.drawString(label, (float) x, (float) y);
			}
		}
		boolean filled = series.get(0).getChartData() instanceof XDDFRadarChartData
				&& ((XDDFRadarChartData) series.get(0).getChartData()).getStyle() == RadarStyle.FILLED;
		for (int k = 0; k < series.size(); k++) {
			PowerpointChartSeries s = series.get(k);
			Color color = lineColor(s, k);
			Path2D shape = polygon(cx, cy, radius, count, s.getValues(), scale);
			if (filled) {
				g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90));
				g.fill(shape);
			}
			g.setColor(color);
			g.setStroke(new BasicStroke((float) Math.max(1.2, metrics.getAscent() * 0.18), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(shape);
		}
	}

	/**
	 * Return the polygon of a radar: a ring when values is null, the polygon of a series otherwise
	 */
	private static Path2D polygon(double cx, double cy, double radius, int count, List<Double> values, Group scale) {
		Path2D path = new Path2D.Double();
		for (int i = 0; i < count; i++) {
			double r = radius;
			if (values != null) {
				Double value = i < values.size() ? values.get(i) : null;
				r = value != null ? (value - scale.min) / (scale.max - scale.min) * radius : 0;
			}
			double angle = Math.toRadians(90 - 360.0 * i / count);
			double x = cx + Math.cos(angle) * r;
			double y = cy - Math.sin(angle) * r;
			if (i == 0) {
				path.moveTo(x, y);
			}
			else {
				path.lineTo(x, y);
			}
		}
		path.closePath();
		return path;
	}

	// ---- Colors and text

	private static Color seriesColor(PowerpointChartSeries s, int index) {
		return s.getColor() != null ? s.getColor() : PALETTE[index % PALETTE.length];
	}

	private static Color lineColor(PowerpointChartSeries s, int index) {
		return s.getLineColor() != null ? s.getLineColor() : PALETTE[index % PALETTE.length];
	}

	private static Color pointColor(PowerpointChartSeries s, int index) {
		List<Color> colors = s.getPointColors();
		return index < colors.size() && colors.get(index) != null ? colors.get(index) : PALETTE[index % PALETTE.length];
	}

	private static Color pointColorOr(PowerpointChartSeries s, int index, Color defaultColor) {
		List<Color> colors = s.getPointColors();
		return index < colors.size() && colors.get(index) != null ? colors.get(index) : defaultColor;
	}

	/**
	 * Draw a text centered on x, shortened to the supplied width when it is positive
	 */
	private void drawText(String text, double x, double baseline, double maxWidth) {
		if (text == null) {
			return;
		}
		FontMetrics metrics = g.getFontMetrics();
		String shown = text;
		if (maxWidth > 0) {
			while (shown.length() > 3 && metrics.stringWidth(shown) > maxWidth) {
				shown = shown.substring(0, shown.length() - 2);
			}
			if (!shown.equals(text)) {
				shown = shown + "…";
			}
		}
		g.drawString(shown, (float) (x - metrics.stringWidth(shown) / 2.0), (float) baseline);
	}
}
