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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawGraphicalFrame;
import org.apache.poi.sl.draw.DrawShape;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.GraphicalFrame;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointChart;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShapeGroup;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;

/**
 * POI draw factory that draws the charts of a slide with {@link PowerpointChartRenderer}: POI itself only draws the fallback picture of a
 * graphic frame, which presentations rarely carry.<br>
 * Install it on a graphics before drawing the slide with {@link #install(Graphics2D, PowerpointSlide)}: charts are then drawn at their
 * place in z-order, inside groups as well.
 * 
 * @author sylvain
 * 
 */
public class PowerpointDrawFactory extends DrawFactory {

	private final PowerpointSlide slide;

	public PowerpointDrawFactory(PowerpointSlide slide) {
		this.slide = slide;
	}

	/**
	 * Make POI draw the charts of the supplied slide on the supplied graphics
	 * 
	 * @param graphics
	 * @param slide
	 */
	public static void install(Graphics2D graphics, PowerpointSlide slide) {
		graphics.setRenderingHint(Drawable.DRAW_FACTORY, new PowerpointDrawFactory(slide));
	}

	@Override
	public DrawGraphicalFrame getDrawable(GraphicalFrame<?, ?> frame) {
		PowerpointChart chart = findChart(slide.getPowerpointShapes(), frame);
		if (chart == null || chart.getSeries().isEmpty()) {
			return super.getDrawable(frame);
		}
		return new DrawGraphicalFrame(frame) {
			@Override
			public void draw(Graphics2D context) {
				// The anchor takes the transformation of the enclosing groups into account
				Rectangle2D anchor = DrawShape.getAnchor(context, frame);
				if (anchor != null) {
					PowerpointChartRenderer.paint(context, chart, anchor);
				}
			}
		};
	}

	private static PowerpointChart findChart(List<PowerpointShape> shapes, GraphicalFrame<?, ?> frame) {
		for (PowerpointShape shape : shapes) {
			if (shape instanceof PowerpointChart && (Object) shape.getShape() == frame) {
				return (PowerpointChart) shape;
			}
			if (shape instanceof PowerpointShapeGroup) {
				PowerpointChart found = findChart(((PowerpointShapeGroup) shape).getChildren(), frame);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
