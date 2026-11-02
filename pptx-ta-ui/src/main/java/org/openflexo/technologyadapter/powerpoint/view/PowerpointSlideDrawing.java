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
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.openflexo.diana.DianaCoreUtils;
import org.openflexo.diana.DianaModelFactory;
import org.openflexo.diana.DrawingGraphicalRepresentation;
import org.openflexo.diana.GRBinding.DrawingGRBinding;
import org.openflexo.diana.GRBinding.ShapeGRBinding;
import org.openflexo.diana.GRProvider.DrawingGRProvider;
import org.openflexo.diana.GRProvider.ShapeGRProvider;
import org.openflexo.diana.GRStructureVisitor;
import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.diana.ShapeGraphicalRepresentation.DimensionConstraints;
import org.openflexo.diana.ShapeGraphicalRepresentation.LocationConstraints;
import org.openflexo.diana.graphics.DianaDrawingDecorationGraphics;
import org.openflexo.diana.graphics.DrawingDecorationPainter;
import org.openflexo.diana.impl.DrawingImpl;
import org.openflexo.diana.shapes.ShapeSpecification.ShapeType;
import org.openflexo.diana.swing.graphics.JDianaGraphics;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShapeGroup;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideElement;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTable;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTableCell;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTableRow;

/**
 * Diana drawing representing a {@link PowerpointSlide}.<br>
 * 
 * POI keeps rendering the slide: the whole slide is painted by a {@link DrawingDecorationPainter}, so that rendering is exactly the one
 * of {@link org.apache.poi.xslf.usermodel.XSLFSlide#draw(Graphics2D)}. Each element of the slide is then represented by a transparent,
 * read-only Diana shape covering its bounds, which gives focus and selection without changing what is displayed. Diana shapes are nested
 * as the elements are: the shapes of a {@link PowerpointShapeGroup} in the shape of the group, the rows of a {@link PowerpointTable} in
 * the shape of the table, and the cells in their row.
 * 
 * @author sylvain
 * 
 */
public class PowerpointSlideDrawing extends DrawingImpl<PowerpointSlide> {

	private static final Logger logger = Logger.getLogger(PowerpointSlideDrawing.class.getPackage().getName());

	/**
	 * Minimal size (in points) of the area representing an element, so that a line or a connector remains clickable
	 */
	private static final double MIN_SIZE = 6.0;

	public PowerpointSlideDrawing(PowerpointSlide slide) {
		super(slide, DianaCoreUtils.TOOLS_FACTORY, PersistenceMode.UniqueGraphicalRepresentations);
		setEditable(false);
	}

	@Override
	public void init() {

		final DrawingGraphicalRepresentation drawingGR = getFactory().makeDrawingGraphicalRepresentation();
		Dimension pageSize = getModel().getSlide().getSlideShow().getPageSize();
		drawingGR.setWidth(pageSize.getWidth());
		drawingGR.setHeight(pageSize.getHeight());
		drawingGR.setDrawWorkingArea(false);
		drawingGR.setDecorationPainter(new SlidePainter());

		// The cells of a table get their location from their text: compute it before building the graphical representations
		updateCellAnchors(getModel().getSlide().getShapes(), new HashSet<>());

		final DrawingGRBinding<PowerpointSlide> slideBinding = bindDrawing(PowerpointSlide.class, "slide",
				new DrawingGRProvider<PowerpointSlide>() {
					@Override
					public DrawingGraphicalRepresentation provideGR(PowerpointSlide drawable, DianaModelFactory factory) {
						return drawingGR;
					}
				});

		final ShapeGRBinding<PowerpointShape> shapeBinding = bindShape(PowerpointShape.class, "shape",
				new ShapeGRProvider<PowerpointShape>() {
					@Override
					public ShapeGraphicalRepresentation provideGR(PowerpointShape drawable, DianaModelFactory factory) {
						return makeGraphicalRepresentation(drawable);
					}
				});

		final ShapeGRBinding<PowerpointTableRow> rowBinding = bindShape(PowerpointTableRow.class, "row",
				new ShapeGRProvider<PowerpointTableRow>() {
					@Override
					public ShapeGraphicalRepresentation provideGR(PowerpointTableRow drawable, DianaModelFactory factory) {
						return makeGraphicalRepresentation(drawable);
					}
				});

		final ShapeGRBinding<PowerpointTableCell> cellBinding = bindShape(PowerpointTableCell.class, "cell",
				new ShapeGRProvider<PowerpointTableCell>() {
					@Override
					public ShapeGraphicalRepresentation provideGR(PowerpointTableCell drawable, DianaModelFactory factory) {
						return makeGraphicalRepresentation(drawable);
					}
				});

		slideBinding.addToWalkers(new GRStructureVisitor<PowerpointSlide>() {
			@Override
			public void visit(PowerpointSlide slide) {
				// Shapes are listed in z-order: the last one drawn is the topmost, and gets the focus first
				for (PowerpointShape shape : slide.getPowerpointShapes()) {
					if (shape.getShape() != null) {
						drawShape(shapeBinding, shape);
					}
				}
			}
		});

		shapeBinding.addToWalkers(new GRStructureVisitor<PowerpointShape>() {
			@Override
			public void visit(PowerpointShape shape) {
				if (shape instanceof PowerpointShapeGroup) {
					for (PowerpointShape child : ((PowerpointShapeGroup) shape).getChildren()) {
						if (child.getShape() != null) {
							drawShape(shapeBinding, child, shape);
						}
					}
				}
				else if (shape instanceof PowerpointTable) {
					for (PowerpointTableRow row : ((PowerpointTable) shape).getRows()) {
						drawShape(rowBinding, row, shape);
					}
				}
			}
		});

		rowBinding.addToWalkers(new GRStructureVisitor<PowerpointTableRow>() {
			@Override
			public void visit(PowerpointTableRow row) {
				for (PowerpointTableCell cell : row.getCells()) {
					if (cell.getCell() != null) {
						drawShape(cellBinding, cell, row);
					}
				}
			}
		});
	}

	private static void updateCellAnchors(Iterable<XSLFShape> shapes, Set<XSLFShape> done) {
		for (XSLFShape shape : shapes) {
			if (shape instanceof XSLFTable && done.add(shape)) {
				try {
					((XSLFTable) shape).updateCellAnchor();
				} catch (RuntimeException e) {
					logger.log(Level.WARNING, "Could not compute the cells of table " + shape.getShapeName(), e);
				}
			}
			else if (shape instanceof XSLFGroupShape) {
				updateCellAnchors(((XSLFGroupShape) shape).getShapes(), done);
			}
		}
	}

	/**
	 * Return the element containing the supplied one in the drawing, or null for a top-level shape
	 * 
	 * @param element
	 * @return
	 */
	private static PowerpointSlideElement getParentElement(PowerpointSlideElement element) {
		if (element instanceof PowerpointShape) {
			return ((PowerpointShape) element).getContainer();
		}
		if (element instanceof PowerpointTableRow) {
			return ((PowerpointTableRow) element).getTable();
		}
		if (element instanceof PowerpointTableCell) {
			return ((PowerpointTableCell) element).getTableRow();
		}
		return null;
	}

	/**
	 * Build a transparent and read-only graphical representation covering the bounds of the element.<br>
	 * The location of a nested element is relative to the Diana shape of its parent element.
	 * 
	 * @param element
	 * @return
	 */
	private ShapeGraphicalRepresentation makeGraphicalRepresentation(PowerpointSlideElement element) {
		ShapeGraphicalRepresentation returned = getFactory().makeShapeGraphicalRepresentation(ShapeType.RECTANGLE);
		Rectangle2D bounds = getBounds(element);
		double x = bounds.getX();
		double y = bounds.getY();
		PowerpointSlideElement parent = getParentElement(element);
		if (parent != null) {
			Rectangle2D parentBounds = getBounds(parent);
			x -= parentBounds.getX();
			y -= parentBounds.getY();
		}
		returned.setX(x);
		returned.setY(y);
		returned.setWidth(bounds.getWidth());
		returned.setHeight(bounds.getHeight());
		returned.setForeground(getFactory().makeNoneForegroundStyle());
		returned.setBackground(getFactory().makeEmptyBackground());
		returned.setShadowStyle(getFactory().makeNoneShadowStyle());
		returned.setIsReadOnly(true);
		// No floating label: an empty floating label would still show its own selection handles
		returned.setIsFloatingLabel(false);
		returned.setLocationConstraints(LocationConstraints.UNMOVABLE);
		returned.setDimensionConstraints(DimensionConstraints.UNRESIZABLE);
		return returned;
	}

	/**
	 * Return the bounds of the element in slide coordinates (points), enlarged to {@link #MIN_SIZE} when too thin to be clicked
	 * 
	 * @param element
	 * @return
	 */
	private static Rectangle2D getBounds(PowerpointSlideElement element) {
		Rectangle2D bounds = null;
		if (element instanceof PowerpointTable && ((PowerpointShape) element).getShape() != null) {
			// The rows of a table may grow with their text beyond the frame of the table
			bounds = getBounds(((PowerpointShape) element).getShape());
			for (PowerpointTableRow row : ((PowerpointTable) element).getRows()) {
				bounds = bounds.createUnion(getBounds(row));
			}
			return bounds;
		}
		if (element instanceof PowerpointShape && ((PowerpointShape) element).getShape() != null) {
			bounds = getBounds(((PowerpointShape) element).getShape());
		}
		else if (element instanceof PowerpointTableRow) {
			for (PowerpointTableCell cell : ((PowerpointTableRow) element).getCells()) {
				bounds = bounds == null ? getBounds(cell) : bounds.createUnion(getBounds(cell));
			}
		}
		else if (element instanceof PowerpointTableCell && ((PowerpointTableCell) element).getCell() != null) {
			PowerpointTableCell cell = (PowerpointTableCell) element;
			XSLFShape table = cell.getTableRow() != null && cell.getTableRow().getTable() != null ? cell.getTableRow().getTable().getShape()
					: null;
			bounds = enlarge(mapToSlide(cell.getCell().getAnchor(), table != null ? table.getParent() : null));
		}
		return bounds != null ? bounds : new Rectangle2D.Double(0, 0, MIN_SIZE, MIN_SIZE);
	}

	/**
	 * Return the bounds of the shape in slide coordinates (points), taking its rotation into account, and enlarged to {@link #MIN_SIZE}
	 * when too thin to be clicked
	 * 
	 * @param shape
	 * @return
	 */
	private static Rectangle2D getBounds(XSLFShape shape) {
		Rectangle2D anchor = mapToSlide(shape.getAnchor(), shape.getParent());
		if (anchor == null) {
			return new Rectangle2D.Double(0, 0, MIN_SIZE, MIN_SIZE);
		}
		if (shape instanceof XSLFSimpleShape) {
			double rotation = ((XSLFSimpleShape) shape).getRotation();
			if (rotation != 0) {
				AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(rotation), anchor.getCenterX(),
						anchor.getCenterY());
				anchor = rotate.createTransformedShape(anchor).getBounds2D();
			}
		}
		return enlarge(anchor);
	}

	private static Rectangle2D enlarge(Rectangle2D anchor) {
		if (anchor == null) {
			return new Rectangle2D.Double(0, 0, MIN_SIZE, MIN_SIZE);
		}
		double width = Math.max(anchor.getWidth(), MIN_SIZE);
		double height = Math.max(anchor.getHeight(), MIN_SIZE);
		return new Rectangle2D.Double(anchor.getCenterX() - width / 2, anchor.getCenterY() - height / 2, width, height);
	}

	/**
	 * Map an anchor expressed in the coordinates of the supplied container onto slide coordinates. The anchor of a shape located in a
	 * group is expressed in the interior coordinates of the group (its child offset and extent), which are mapped onto the anchor of the
	 * group.
	 * 
	 * @param anchor
	 * @param container
	 * @return
	 */
	private static Rectangle2D mapToSlide(Rectangle2D anchor, ShapeContainer<?, ?> container) {
		if (anchor == null || !(container instanceof XSLFGroupShape)) {
			return anchor;
		}
		XSLFGroupShape group = (XSLFGroupShape) container;
		Rectangle2D groupAnchor = mapToSlide(group.getAnchor(), group.getParent());
		Rectangle2D interior = group.getInteriorAnchor();
		if (groupAnchor == null || interior == null) {
			return anchor;
		}
		double scaleX = interior.getWidth() != 0 ? groupAnchor.getWidth() / interior.getWidth() : 1;
		double scaleY = interior.getHeight() != 0 ? groupAnchor.getHeight() / interior.getHeight() : 1;
		return new Rectangle2D.Double(groupAnchor.getX() + (anchor.getX() - interior.getX()) * scaleX,
				groupAnchor.getY() + (anchor.getY() - interior.getY()) * scaleY, anchor.getWidth() * scaleX, anchor.getHeight() * scaleY);
	}

	/**
	 * Paint the whole slide with POI, below the Diana shapes
	 */
	private class SlidePainter implements DrawingDecorationPainter {

		@Override
		public void paintDecoration(DianaDrawingDecorationGraphics g) {
			if (!(g instanceof JDianaGraphics)) {
				return;
			}
			Graphics2D g2 = (Graphics2D) ((JDianaGraphics) g).getGraphics().create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				g2.scale(g.getScale(), g.getScale());
				getModel().getSlide().draw(g2);
			} catch (RuntimeException e) {
				// POI may fail on some contents (comments, unsupported parts): do not break the whole view
				logger.log(Level.WARNING, "Could not render slide " + getModel().getName(), e);
			} finally {
				g2.dispose();
			}
		}

		@Override
		public boolean paintBeforeDrawing() {
			return false;
		}
	}
}
