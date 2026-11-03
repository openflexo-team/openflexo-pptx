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

import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.xmlbeans.XmlCursor;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Represents a PowerPoint shape, as a PAMELA entity wrapping the POI {@link XSLFShape} concept.
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
public interface PowerpointShape extends PowerpointSlideElement {

	@PropertyIdentifier(type = PowerpointSlide.class)
	public static final String SLIDE_KEY = "powerpointSlide";
	@PropertyIdentifier(type = XSLFShape.class)
	public static final String SHAPE_KEY = "shape";
	@PropertyIdentifier(type = PowerpointShapeGroup.class)
	public static final String CONTAINER_KEY = "container";

	/**
	 * Return {@link PowerpointSlide} where this {@link PowerpointShape} is defined
	 *
	 * @return
	 */
	@Getter(value = SLIDE_KEY, inverse = PowerpointSlide.POWERPOINT_SHAPES_KEY)
	public PowerpointSlide getPowerpointSlide();

	@Setter(SLIDE_KEY)
	public void setPowerpointSlide(PowerpointSlide slide);

	/**
	 * Return the {@link PowerpointShapeGroup} containing this shape, or null when the shape is located at the top level of its slide
	 *
	 * @return
	 */
	@Getter(value = CONTAINER_KEY, inverse = PowerpointShapeGroup.CHILDREN_KEY)
	public PowerpointShapeGroup getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(PowerpointShapeGroup container);

	/**
	 * Return the POI {@link XSLFShape} wrapped by this {@link PowerpointShape}
	 *
	 * @return
	 */
	@Getter(value = SHAPE_KEY, ignoreType = true)
	public XSLFShape getShape();

	@Setter(SHAPE_KEY)
	public void setShape(XSLFShape shape);

	public double getX();

	public double getY();

	public double getWidth();

	public double getHeight();

	/**
	 * Return the text carried by this shape, or null when the shape holds no text.<br>
	 * Declared here (rather than only on {@link PowerpointTextShape}) so FML bindings can navigate
	 * {@code shape.text} on any shape reference.
	 *
	 * @return
	 */
	public String getText();

	/**
	 * Sets the text carried by this shape (no-op for non-text shapes)
	 *
	 * @param text
	 */
	public void setText(String text);

	/**
	 * Return the identifier of this shape in its slide (the id of its non-visual properties), used by connectors to designate the shapes
	 * they are glued to
	 *
	 * @return
	 */
	public int getShapeId();

	/**
	 * Return the alternative text (description) of this shape, or null when it has none
	 *
	 * @return
	 */
	public String getAltText();

	/**
	 * Default base implementation for {@link PowerpointShape}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointShapeImpl extends PowerpointObjectImpl implements PowerpointShape {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointShapeImpl.class.getPackage().getName());

		@Override
		public int getShapeId() {
			return getShape() != null ? getShape().getShapeId() : -1;
		}

		@Override
		public String getAltText() {
			if (getShape() == null) {
				return null;
			}
			// <p:sp><p:nvSpPr><p:cNvPr descr="..."/> : the same structure for every kind of shape
			try (XmlCursor cursor = getShape().getXmlObject().newCursor()) {
				if (cursor.toFirstChild() && cursor.toFirstChild()) {
					String descr = cursor.getAttributeText(new QName("descr"));
					return descr != null && !descr.isEmpty() ? descr : null;
				}
			}
			return null;
		}

		/**
		 * A shape located in a group is not referenced by its slide: return the slide of its container
		 */
		@Override
		public PowerpointSlide getPowerpointSlide() {
			if (getContainer() != null) {
				return getContainer().getPowerpointSlide();
			}
			return (PowerpointSlide) performSuperGetter(SLIDE_KEY);
		}

		@Override
		public PowerpointSlideshow getResourceData() {
			return getPowerpointSlide() != null ? getPowerpointSlide().getPowerpointSlideshow() : null;
		}

		@Override
		public String getName() {
			return getShape() != null ? getShape().getShapeName() : null;
		}

		@Override
		public String getUri() {
			if (getContainer() != null) {
				return getContainer().getUri() + PowerpointObjectURI.shapeSegment(this, getContainer().getChildren());
			}
			PowerpointSlide slide = getPowerpointSlide();
			if (slide == null) {
				return "pptx:slideshow" + PowerpointObjectURI.PATH_SEPARATOR + PowerpointObjectURI.shapeSegment(this, null);
			}
			return slide.getUri() + PowerpointObjectURI.shapeSegment(this, slide.getPowerpointShapes());
		}

		private Rectangle2D getAnchor() {
			return getShape() != null ? getShape().getAnchor() : null;
		}

		@Override
		public double getX() {
			return getAnchor() != null ? getAnchor().getX() : 0;
		}

		@Override
		public double getY() {
			return getAnchor() != null ? getAnchor().getY() : 0;
		}

		@Override
		public double getWidth() {
			return getAnchor() != null ? getAnchor().getWidth() : 0;
		}

		@Override
		public double getHeight() {
			return getAnchor() != null ? getAnchor().getHeight() : 0;
		}

		@Override
		public String getText() {
			// Non-text shapes carry no text ; overridden by PowerpointTextShape
			return null;
		}

		@Override
		public void setText(String text) {
			// No-op for non-text shapes ; overridden by PowerpointTextShape
		}

	}

}
