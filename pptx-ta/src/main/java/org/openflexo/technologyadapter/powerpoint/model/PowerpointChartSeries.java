/**
 *
 * Copyright (c) 2014-2025, Openflexo
 *
 * This file is part of Powerpointconnector, a component of the software infrastructure
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

package org.openflexo.technologyadapter.powerpoint.model;

import static org.openflexo.technologyadapter.powerpoint.model.PowerpointXmlUtils.CHART_NAMESPACE;
import static org.openflexo.technologyadapter.powerpoint.model.PowerpointXmlUtils.findAttribute;
import static org.openflexo.technologyadapter.powerpoint.model.PowerpointXmlUtils.firstChild;
import static org.openflexo.technologyadapter.powerpoint.model.PowerpointXmlUtils.lineColor;
import static org.openflexo.technologyadapter.powerpoint.model.PowerpointXmlUtils.solidFillColor;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

/**
 * Represents a series of a {@link PowerpointChart}: a name, the categories and the values read from the cache of the chart
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointChartSeries.PowerpointChartSeriesImpl.class)
public interface PowerpointChartSeries extends PowerpointSlideElement {

	@PropertyIdentifier(type = PowerpointChart.class)
	public static final String CHART_KEY = "chart";
	@PropertyIdentifier(type = XDDFChartData.class)
	public static final String CHART_DATA_KEY = "chartData";
	@PropertyIdentifier(type = Integer.class)
	public static final String SERIES_INDEX_KEY = "seriesIndex";

	@Getter(value = CHART_KEY, inverse = PowerpointChart.SERIES_KEY)
	public PowerpointChart getChart();

	@Setter(CHART_KEY)
	public void setChart(PowerpointChart chart);

	/**
	 * Return the POI chart data (one kind of chart of the plot area) this series belongs to
	 *
	 * @return
	 */
	@Getter(value = CHART_DATA_KEY, ignoreType = true)
	public XDDFChartData getChartData();

	@Setter(CHART_DATA_KEY)
	public void setChartData(XDDFChartData chartData);

	/**
	 * Return the index of this series in its chart data
	 *
	 * @return
	 */
	@Getter(value = SERIES_INDEX_KEY, defaultValue = "0")
	public int getSeriesIndex();

	@Setter(SERIES_INDEX_KEY)
	public void setSeriesIndex(int index);

	/**
	 * Return the kind of chart this series is drawn with (for instance "bar", "column", "line", "doughnut")
	 *
	 * @return
	 */
	public String getChartType();

	/**
	 * Return the categories of this series (the labels of its points)
	 *
	 * @return
	 */
	public List<String> getCategories();

	/**
	 * Return the values of this series, in the order of its categories (null for a missing value)
	 *
	 * @return
	 */
	public List<Double> getValues();

	/**
	 * Return the value of this series for the supplied category, or null
	 *
	 * @param category
	 * @return
	 */
	public Double getValue(String category);

	/**
	 * Return the color of this series (its solid fill, or its line for a line series), or null when it is not given explicitly
	 *
	 * @return
	 */
	public Color getColor();

	/**
	 * Return the color of the line of this series (for a line or radar series), or its fill color when it has no line color
	 *
	 * @return
	 */
	public Color getLineColor();

	/**
	 * Return the colors given to the points of this series (the slices of a pie, for instance), in the order of its categories, with
	 * null for a point without its own color
	 *
	 * @return
	 */
	public List<Color> getPointColors();

	/**
	 * Return whether the values of this series are shown as labels on the chart
	 *
	 * @return
	 */
	public boolean isShowValues();

	/**
	 * Return the points of this series as text, one "category: value" per line
	 *
	 * @return
	 */
	public String getDataText();

	/**
	 * Default base implementation for {@link PowerpointChartSeries}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointChartSeriesImpl extends PowerpointObjectImpl implements PowerpointChartSeries {

		private static final Logger logger = Logger.getLogger(PowerpointChartSeriesImpl.class.getPackage().getName());

		@Override
		public PowerpointSlide getPowerpointSlide() {
			return getChart() != null ? getChart().getPowerpointSlide() : null;
		}

		@Override
		public PowerpointSlideshow getResourceData() {
			return getChart() != null ? getChart().getResourceData() : null;
		}

		private XDDFChartData.Series getPOISeries() {
			if (getChartData() == null || getSeriesIndex() < 0 || getSeriesIndex() >= getChartData().getSeriesCount()) {
				return null;
			}
			return getChartData().getSeries(getSeriesIndex());
		}

		/**
		 * Invoke a protected accessor of the POI series: the name of a series and the XML of its categories are not public in POI
		 */
		private Object invokeSeriesAccessor(String methodName) {
			XDDFChartData.Series series = getPOISeries();
			if (series == null) {
				return null;
			}
			try {
				Method method = XDDFChartData.Series.class.getDeclaredMethod(methodName);
				method.setAccessible(true);
				return method.invoke(series);
			} catch (ReflectiveOperationException | RuntimeException e) {
				logger.log(Level.FINE, "Could not invoke " + methodName + " on a series", e);
				return null;
			}
		}

		@Override
		public String getName() {
			Object seriesText = invokeSeriesAccessor("getSeriesText");
			if (seriesText instanceof CTSerTx) {
				try {
					CTSerTx text = (CTSerTx) seriesText;
					if (text.isSetStrRef() && text.getStrRef().isSetStrCache() && text.getStrRef().getStrCache().sizeOfPtArray() > 0) {
						return text.getStrRef().getStrCache().getPtArray(0).getV();
					}
					if (text.isSetV()) {
						return text.getV();
					}
				} catch (RuntimeException e) {
					logger.log(Level.FINE, "Could not read the name of a series", e);
				}
			}
			return "Series " + (getChart() != null ? getChart().getSeries().indexOf(this) + 1 : 0);
		}

		@Override
		public String getUri() {
			return (getChart() != null ? getChart().getUri() : "pptx:chart") + "/series["
					+ (getChart() != null ? getChart().getSeries().indexOf(this) : -1) + "]";
		}

		@Override
		public String getChartType() {
			if (getChartData() == null) {
				return null;
			}
			if (getChartData() instanceof XDDFBarChartData && ((XDDFBarChartData) getChartData()).getBarDirection() != null) {
				// A vertical bar chart is called a column chart
				return ((XDDFBarChartData) getChartData()).getBarDirection().name().equals("COL") ? "column" : "bar";
			}
			return getChartData().getClass().getSimpleName().replace("XDDF", "").replace("ChartData", "").toLowerCase();
		}

		@Override
		public List<String> getCategories() {
			List<String> returned = new ArrayList<>();
			XDDFChartData.Series series = getPOISeries();
			XDDFDataSource<?> categories = series != null ? series.getCategoryData() : null;
			if (categories != null) {
				for (int i = 0; i < categories.getPointCount(); i++) {
					returned.add(categoryLabel(categories.getPointAt(i)));
				}
			}
			if (returned.isEmpty() && invokeSeriesAccessor("getAxDS") instanceof XmlObject) {
				// POI does not read multi-level categories (multiLvlStrRef): read the labels of their first level in the cache
				try (XmlCursor cursor = ((XmlObject) invokeSeriesAccessor("getAxDS")).newCursor(); XmlCursor end = cursor.newCursor()) {
					end.toEndToken();
					int levels = 0;
					while (cursor.toNextToken() != XmlCursor.TokenType.NONE && cursor.isLeftOf(end)) {
						if (cursor.isStart()) {
							String name = cursor.getName().getLocalPart();
							if ("lvl".equals(name) && ++levels > 1) {
								break;
							}
							if ("v".equals(name)) {
								returned.add(cursor.getTextValue());
							}
						}
					}
				}
			}
			return returned;
		}

		/**
		 * Numerical categories (years, for instance) are read as doubles: show the integral ones without decimals
		 */
		private static String categoryLabel(Object point) {
			if (point instanceof Number) {
				double value = ((Number) point).doubleValue();
				if (value == Math.rint(value) && !Double.isInfinite(value)) {
					return String.valueOf((long) value);
				}
			}
			return point != null ? point.toString() : null;
		}

		@Override
		public List<Double> getValues() {
			List<Double> returned = new ArrayList<>();
			XDDFChartData.Series series = getPOISeries();
			XDDFDataSource<? extends Number> values = series != null ? series.getValuesData() : null;
			if (values != null) {
				for (int i = 0; i < values.getPointCount(); i++) {
					Number point = values.getPointAt(i);
					returned.add(point != null ? point.doubleValue() : null);
				}
			}
			return returned;
		}

		@Override
		public Double getValue(String category) {
			int index = getCategories().indexOf(category);
			List<Double> values = getValues();
			return index >= 0 && index < values.size() ? values.get(index) : null;
		}

		/**
		 * Return the XML element of this series (c:ser), the parent of its values
		 */
		private XmlObject getSeriesXml() {
			Object values = invokeSeriesAccessor("getNumDS");
			if (!(values instanceof XmlObject)) {
				return null;
			}
			try (XmlCursor cursor = ((XmlObject) values).newCursor()) {
				return cursor.toParent() ? cursor.getObject() : null;
			}
		}

		@Override
		public Color getColor() {
			XmlObject shapeProperties = firstChild(getSeriesXml(), CHART_NAMESPACE, "spPr");
			Color fill = solidFillColor(shapeProperties);
			return fill != null ? fill : lineColor(shapeProperties);
		}

		@Override
		public Color getLineColor() {
			XmlObject shapeProperties = firstChild(getSeriesXml(), CHART_NAMESPACE, "spPr");
			Color line = lineColor(shapeProperties);
			return line != null ? line : solidFillColor(shapeProperties);
		}

		@Override
		public List<Color> getPointColors() {
			List<Color> returned = new ArrayList<>();
			for (int i = 0; i < getValues().size(); i++) {
				returned.add(null);
			}
			XmlObject series = getSeriesXml();
			if (series != null) {
				for (XmlObject point : series.selectChildren(CHART_NAMESPACE, "dPt")) {
					String index = findAttribute(point, "idx", "val");
					try {
						int i = Integer.parseInt(index);
						if (i >= 0 && i < returned.size()) {
							returned.set(i, solidFillColor(firstChild(point, CHART_NAMESPACE, "spPr")));
						}
					} catch (NumberFormatException e) {
						// Ignore a point without index
					}
				}
			}
			return returned;
		}

		@Override
		public boolean isShowValues() {
			return "1".equals(findAttribute(firstChild(getSeriesXml(), CHART_NAMESPACE, "dLbls"), "showVal", "val"));
		}

		@Override
		public String getDataText() {
			StringBuilder returned = new StringBuilder();
			List<String> categories = getCategories();
			List<Double> values = getValues();
			for (int i = 0; i < Math.max(categories.size(), values.size()); i++) {
				returned.append(i < categories.size() ? categories.get(i) : "?").append(": ").append(i < values.size() ? values.get(i) : "")
						.append('\n');
			}
			return returned.toString();
		}
	}
}
