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

import java.awt.Color;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XSLFChart;
import org.apache.poi.xslf.usermodel.XSLFGraphicFrame;
import org.apache.xmlbeans.XmlCursor;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend;

/**
 * Represents a chart of a slide: a graphic frame holding a POI {@link XSLFChart}
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointChart.PowerpointChartImpl.class)
public interface PowerpointChart extends PowerpointShape {

	@PropertyIdentifier(type = PowerpointChartSeries.class, cardinality = Cardinality.LIST)
	public static final String SERIES_KEY = "series";

	/**
	 * Return the series of this chart, in the order of the plot area
	 *
	 * @return
	 */
	@Getter(value = SERIES_KEY, cardinality = Cardinality.LIST, inverse = PowerpointChartSeries.CHART_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<PowerpointChartSeries> getSeries();

	@Setter(SERIES_KEY)
	public void setSeries(List<PowerpointChartSeries> series);

	@Adder(SERIES_KEY)
	@PastingPoint
	public void addToSeries(PowerpointChartSeries aSeries);

	@Remover(SERIES_KEY)
	public void removeFromSeries(PowerpointChartSeries aSeries);

	/**
	 * Return the POI {@link XSLFChart} displayed by this chart, or null
	 *
	 * @return
	 */
	public XSLFChart getChart();

	/**
	 * Return the title of this chart, or null when it has none
	 *
	 * @return
	 */
	public String getTitle();

	/**
	 * Return the kinds of chart used by this chart (for instance "bar" or "bar, line" for a combination chart)
	 *
	 * @return
	 */
	public String getChartType();

	/**
	 * Return the position of the legend of this chart ("bottom", "top", "left", "right" or "top right"), or null when it has none
	 *
	 * @return
	 */
	public String getLegendPosition();

	/**
	 * Return the color of the texts of this chart (axes, legend, labels), or null when it is not given explicitly
	 *
	 * @return
	 */
	public Color getTextColor();

	/**
	 * Return the solid fill of the plot area of this chart, or null when it has none
	 *
	 * @return
	 */
	public Color getPlotAreaColor();

	/**
	 * Return the series with the supplied name, or null
	 *
	 * @param name
	 * @return
	 */
	public PowerpointChartSeries getSeries(String name);

	/**
	 * Default base implementation for {@link PowerpointChart}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointChartImpl extends PowerpointShapeImpl implements PowerpointChart {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointChartImpl.class.getPackage().getName());

		@Override
		public XSLFChart getChart() {
			if (getShape() instanceof XSLFGraphicFrame && ((XSLFGraphicFrame) getShape()).hasChart()) {
				return ((XSLFGraphicFrame) getShape()).getChart();
			}
			return null;
		}

		@Override
		public String getTitle() {
			if (getChart() == null || !getChart().getCTChart().isSetTitle()) {
				return null;
			}
			// The title is rich text: concatenate its runs
			StringBuilder title = new StringBuilder();
			try (XmlCursor cursor = getChart().getCTChart().getTitle().newCursor(); XmlCursor end = cursor.newCursor()) {
				end.toEndToken();
				while (cursor.toNextToken() != XmlCursor.TokenType.NONE && cursor.isLeftOf(end)) {
					if (cursor.isStart() && "t".equals(cursor.getName().getLocalPart())) {
						title.append(cursor.getTextValue());
					}
				}
			}
			return title.length() > 0 ? title.toString() : null;
		}

		@Override
		public String getChartType() {
			StringBuilder returned = new StringBuilder();
			for (PowerpointChartSeries series : getSeries()) {
				if (series.getChartType() != null && returned.indexOf(series.getChartType()) < 0) {
					returned.append(returned.length() > 0 ? ", " : "").append(series.getChartType());
				}
			}
			return returned.length() > 0 ? returned.toString() : null;
		}

		@Override
		public String getLegendPosition() {
			if (getChart() == null || !getChart().getCTChart().isSetLegend()) {
				return null;
			}
			CTLegend legend = getChart().getCTChart().getLegend();
			if (!legend.isSetLegendPos() || legend.getLegendPos().getVal() == null) {
				// The default position of a legend is on the right
				return "right";
			}
			switch (legend.getLegendPos().getVal().toString()) {
				case "b":
					return "bottom";
				case "t":
					return "top";
				case "l":
					return "left";
				case "tr":
					return "top right";
				default:
					return "right";
			}
		}

		@Override
		public Color getTextColor() {
			if (getChart() == null || !getChart().getCTChartSpace().isSetTxPr()) {
				return null;
			}
			return PowerpointXmlUtils.toColor(PowerpointXmlUtils.findAttribute(getChart().getCTChartSpace().getTxPr(), "srgbClr", "val"));
		}

		@Override
		public Color getPlotAreaColor() {
			if (getChart() == null || getChart().getCTChart().getPlotArea() == null || !getChart().getCTChart().getPlotArea().isSetSpPr()) {
				return null;
			}
			return PowerpointXmlUtils.solidFillColor(getChart().getCTChart().getPlotArea().getSpPr());
		}

		@Override
		public PowerpointChartSeries getSeries(String name) {
			for (PowerpointChartSeries series : getSeries()) {
				if (name != null && name.equals(series.getName())) {
					return series;
				}
			}
			return null;
		}
	}
}
