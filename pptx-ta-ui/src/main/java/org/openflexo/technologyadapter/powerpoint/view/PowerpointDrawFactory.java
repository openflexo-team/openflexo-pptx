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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.sl.draw.DrawBackground;
import org.apache.poi.sl.draw.DrawConnectorShape;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawFreeformShape;
import org.apache.poi.sl.draw.DrawGraphicalFrame;
import org.apache.poi.sl.draw.DrawShape;
import org.apache.poi.sl.draw.DrawTextBox;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.apache.poi.sl.draw.DrawTextShape;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.draw.geom.Outline;
import org.apache.poi.sl.usermodel.Background;
import org.apache.poi.sl.usermodel.ConnectorShape;
import org.apache.poi.sl.usermodel.FreeformShape;
import org.apache.poi.sl.usermodel.GraphicalFrame;
import org.apache.poi.sl.usermodel.TextBox;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.xslf.usermodel.XSLFBackground;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.xmlbeans.XmlObject;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointChart;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShapeGroup;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;

/**
 * POI draw factory completing what POI does not render:
 * <ul>
 * <li>the charts of a slide, drawn with {@link PowerpointChartRenderer}: POI itself only draws the fallback picture of a graphic frame,
 * which presentations rarely carry;</li>
 * <li>the pattern fills of the backgrounds and of the shapes, painted with {@link PowerpointPatternFill}: POI ignores them.</li>
 * </ul>
 * It also lays out the paragraphs of a slide as POI draws them, to make them selectable ({@link #layoutParagraphs(PowerpointSlide)}).
 * Install it on a graphics before drawing the slide with {@link #install(Graphics2D, PowerpointSlide)}: charts are then drawn at their
 * place in z-order, inside groups as well.
 * 
 * @author sylvain
 * 
 */
public class PowerpointDrawFactory extends DrawFactory {

	private static final Logger logger = Logger.getLogger(PowerpointDrawFactory.class.getPackage().getName());

	private final PowerpointSlide slide;

	/**
	 * When not null, the bounds of the paragraphs drawn are recorded there, by POI paragraph
	 */
	private final Map<Object, Rectangle2D> paragraphBounds;

	public PowerpointDrawFactory(PowerpointSlide slide) {
		this(slide, null);
	}

	private PowerpointDrawFactory(PowerpointSlide slide, Map<Object, Rectangle2D> paragraphBounds) {
		this.slide = slide;
		this.paragraphBounds = paragraphBounds;
	}

	/**
	 * Lay out the text of the supplied slide as POI draws it, and return the bounds of each paragraph, in slide coordinates (points),
	 * by POI paragraph. The slide is drawn once on an image outside of the screen; empty paragraphs have no bounds.
	 * 
	 * @param slide
	 * @return
	 */
	public static Map<Object, Rectangle2D> layoutParagraphs(PowerpointSlide slide) {
		Map<Object, Rectangle2D> returned = new IdentityHashMap<>();
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			graphics.setRenderingHint(Drawable.DRAW_FACTORY, new PowerpointDrawFactory(slide, returned));
			slide.getSlide().draw(graphics);
		} catch (RuntimeException e) {
			// Laying out is only used to select the paragraphs: a failure only leaves some of them without bounds
			logger.log(Level.WARNING, "Could not lay out the paragraphs of slide " + slide.getName(), e);
		} finally {
			graphics.dispose();
		}
		return returned;
	}

	@Override
	public DrawTextParagraph getDrawable(TextParagraph<?, ?, ?> textParagraph) {
		if (paragraphBounds == null) {
			return super.getDrawable(textParagraph);
		}
		// A paragraph recording where POI places it: setPosition() gives its top left corner, and draw() leaves its height in getY()
		return new DrawTextParagraph(textParagraph) {
			private double left;
			private double top;

			@Override
			public void setPosition(double x, double y) {
				super.setPosition(x, y);
				left = x;
				top = y;
			}

			@Override
			public void draw(Graphics2D graphics) {
				super.draw(graphics);
				if (isEmptyParagraph() || !(paragraph instanceof XSLFTextParagraph)) {
					return;
				}
				XSLFTextShape textShape = ((XSLFTextParagraph) paragraph).getParentShape();
				Rectangle2D anchor = DrawShape.getAnchor(graphics, textShape);
				double right = anchor != null ? anchor.getMaxX() - textShape.getInsets().right : left;
				Rectangle2D bounds = new Rectangle2D.Double(left, top, Math.max(1, right - left), getY());
				// The graphics carries the rotation and flips of the shape: bounds are kept in slide coordinates
				paragraphBounds.put(paragraph, graphics.getTransform().createTransformedShape(bounds).getBounds2D());
			}
		};
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

	@Override
	public DrawBackground getDrawable(Background<?, ?> background) {
		return new DrawBackground(background) {
			@Override
			public void draw(Graphics2D graphics) {
				XmlObject patternFill = PowerpointPatternFill.findPatternFill(((XSLFBackground) getShape()).getXmlObject(), "bgPr");
				if (patternFill == null) {
					super.draw(graphics);
					return;
				}
				Dimension page = getShape().getSheet().getSlideShow().getPageSize();
				Paint oldPaint = graphics.getPaint();
				graphics.setPaint(PowerpointPatternFill.makePaint(patternFill, graphics));
				graphics.fill(new Rectangle2D.Double(0, 0, page.getWidth(), page.getHeight()));
				graphics.setPaint(oldPaint);
			}
		};
	}

	@Override
	public DrawTextBox getDrawable(TextBox<?, ?> shape) {
		return new DrawTextBox(shape) {
			@Override
			public void draw(Graphics2D graphics) {
				if (getAnchor(graphics, getShape()) != null) {
					fillWithPattern(getShape(), graphics, g -> computeOutlines(g));
				}
				super.draw(graphics);
			}
		};
	}

	@Override
	public DrawFreeformShape getDrawable(FreeformShape<?, ?> shape) {
		return new DrawFreeformShape(shape) {
			@Override
			public void draw(Graphics2D graphics) {
				if (getAnchor(graphics, getShape()) != null) {
					fillWithPattern(getShape(), graphics, g -> computeOutlines(g));
				}
				super.draw(graphics);
			}
		};
	}

	@Override
	public DrawTextShape getDrawable(TextShape<?, ?> shape) {
		return new DrawTextShape(shape) {
			@Override
			public void draw(Graphics2D graphics) {
				if (getAnchor(graphics, getShape()) != null) {
					fillWithPattern(getShape(), graphics, g -> computeOutlines(g));
				}
				super.draw(graphics);
			}
		};
	}

	@Override
	public DrawConnectorShape getDrawable(ConnectorShape<?, ?> shape) {
		return new DrawConnectorShape(shape) {
			@Override
			public void draw(Graphics2D graphics) {
				if (getAnchor(graphics, getShape()) != null) {
					fillWithPattern(getShape(), graphics, g -> computeOutlines(g));
				}
				super.draw(graphics);
			}
		};
	}

	/**
	 * Fill the outline of the shape with its pattern, when it is filled with a pattern. POI then draws the rest of the shape (its text
	 * and its outline): it finds no fill style for a pattern, so it does not fill the shape again.<br>
	 * The outlines are supplied by a lambda calling the protected computeOutlines() of POI: the Eclipse compiler does not accept a method
	 * reference to it.
	 */
	private static void fillWithPattern(Object shape, Graphics2D graphics, Function<Graphics2D, Collection<Outline>> outlines) {
		if (!(shape instanceof XSLFShape)) {
			return;
		}
		XmlObject patternFill = PowerpointPatternFill.findPatternFill(((XSLFShape) shape).getXmlObject(), "spPr");
		if (patternFill == null) {
			return;
		}
		Path2D area = new Path2D.Double();
		for (Outline outline : outlines.apply(graphics)) {
			if (outline.getPath().isFilled()) {
				area.append(outline.getOutline(), false);
			}
		}
		Paint oldPaint = graphics.getPaint();
		graphics.setPaint(PowerpointPatternFill.makePaint(patternFill, graphics));
		graphics.fill(area);
		graphics.setPaint(oldPaint);
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
