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
import java.awt.geom.RoundRectangle2D;

import org.openflexo.diana.graphics.DianaShapeGraphics;
import org.openflexo.diana.graphics.ShapePainter;
import org.openflexo.diana.swing.graphics.JDianaGraphics;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointChart;

/**
 * Paints a placeholder in place of a {@link PowerpointChart}, which POI does not render: a dashed frame with the title (or the name) of
 * the chart, its kind and its number of series
 * 
 * @author sylvain
 * 
 */
public class PowerpointChartPlaceholderPainter implements ShapePainter {

	private static final Color FRAME_COLOR = new Color(0xD2, 0x47, 0x26);
	private static final Color FILL_COLOR = new Color(0xD2, 0x47, 0x26, 18);
	private static final Color TEXT_COLOR = new Color(0x55, 0x55, 0x55);

	private final PowerpointChart chart;

	public PowerpointChartPlaceholderPainter(PowerpointChart chart) {
		this.chart = chart;
	}

	@Override
	public void paintShape(DianaShapeGraphics g) {
		if (!(g instanceof JDianaGraphics)) {
			return;
		}
		double scale = g.getScale();
		double width = g.getNode().getWidth() * scale;
		double height = g.getNode().getHeight() * scale;
		Graphics2D g2 = (Graphics2D) ((JDianaGraphics) g).getGraphics().create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			RoundRectangle2D frame = new RoundRectangle2D.Double(1, 1, width - 2, height - 2, 8 * scale, 8 * scale);
			g2.setColor(FILL_COLOR);
			g2.fill(frame);
			g2.setColor(FRAME_COLOR);
			g2.setStroke(new BasicStroke((float) Math.max(1, scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1,
					new float[] { (float) (6 * scale), (float) (4 * scale) }, 0));
			g2.draw(frame);

			String title = chart.getTitle() != null ? chart.getTitle() : chart.getName();
			String kind = (chart.getChartType() != null ? chart.getChartType() + " chart" : "chart") + " · " + chart.getSeries().size()
					+ " series";
			Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, (int) Math.max(8, 14 * scale));
			Font kindFont = new Font(Font.SANS_SERIF, Font.PLAIN, (int) Math.max(7, 11 * scale));
			g2.setColor(TEXT_COLOR);
			drawCentered(g2, title, titleFont, width, height / 2 - 4 * scale);
			drawCentered(g2, kind, kindFont, width, height / 2 + 14 * scale);
		} finally {
			g2.dispose();
		}
	}

	private static void drawCentered(Graphics2D g2, String text, Font font, double width, double baseline) {
		if (text == null) {
			return;
		}
		g2.setFont(font);
		FontMetrics metrics = g2.getFontMetrics();
		String shown = text;
		while (shown.length() > 3 && metrics.stringWidth(shown) > width - 8) {
			shown = shown.substring(0, shown.length() - 2);
		}
		if (shown != text) {
			shown = shown + "…";
		}
		g2.drawString(shown, (float) ((width - metrics.stringWidth(shown)) / 2), (float) baseline);
	}
}
