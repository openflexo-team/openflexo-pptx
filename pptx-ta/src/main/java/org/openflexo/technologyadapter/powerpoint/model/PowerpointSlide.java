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
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
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
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;

/**
 * Represents a PowerPoint slide, as a PAMELA entity wrapping the POI {@link XSLFSlide} concept.
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointSlide.PowerpointSlideImpl.class)
public interface PowerpointSlide extends PowerpointObject {

	@PropertyIdentifier(type = PowerpointSlideshow.class)
	public static final String SLIDE_SHOW_KEY = "powerpointSlideshow";
	@PropertyIdentifier(type = XSLFSlide.class)
	public static final String SLIDE_KEY = "slide";
	@PropertyIdentifier(type = PowerpointShape.class, cardinality = Cardinality.LIST)
	public static final String POWERPOINT_SHAPES_KEY = "powerpointShapes";

	/**
	 * Return {@link PowerpointSlideshow} where this {@link PowerpointSlide} is defined
	 *
	 * @return
	 */
	@Getter(value = SLIDE_SHOW_KEY, inverse = PowerpointSlideshow.POWERPOINT_SLIDES_KEY)
	public PowerpointSlideshow getPowerpointSlideshow();

	@Setter(SLIDE_SHOW_KEY)
	public void setPowerpointSlideshow(PowerpointSlideshow slideshow);

	/**
	 * Return the POI {@link XSLFSlide} wrapped by this {@link PowerpointSlide}
	 *
	 * @return
	 */
	@Getter(value = SLIDE_KEY, ignoreType = true)
	public XSLFSlide getSlide();

	@Setter(SLIDE_KEY)
	public void setSlide(XSLFSlide slide);

	/**
	 * Return all {@link PowerpointShape} defined in this {@link PowerpointSlide}
	 *
	 * @return
	 */
	@Getter(value = POWERPOINT_SHAPES_KEY, cardinality = Cardinality.LIST, inverse = PowerpointShape.SLIDE_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<PowerpointShape> getPowerpointShapes();

	@Setter(POWERPOINT_SHAPES_KEY)
	public void setPowerpointShapes(List<PowerpointShape> shapes);

	@Adder(POWERPOINT_SHAPES_KEY)
	@PastingPoint
	public void addToPowerpointShapes(PowerpointShape aShape);

	@Remover(POWERPOINT_SHAPES_KEY)
	public void removeFromPowerpointShapes(PowerpointShape aShape);

	/**
	 * Return 0-based index of this slide in the enclosing presentation
	 *
	 * @return
	 */
	public int getSlideNumber();

	/**
	 * Return the identifier of this slide in its presentation (the <code>id</code> of its <code>p:sldId</code> in
	 * <code>presentation.xml</code>), or -1.<br>
	 * Unlike the slide number, this identifier does not change when the slides are reordered: it identifies the slide in the URI of its
	 * elements.
	 *
	 * @return
	 */
	public long getSlideId();

	/**
	 * Return the title of this slide (text of the title placeholder), or null when there is none
	 *
	 * @return
	 */
	public String getTitle();

	/**
	 * Create a new text box in this slide, positioned with supplied geometry, containing supplied text
	 *
	 * @param text
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return the newly created {@link PowerpointTextBox}
	 */
	public PowerpointTextBox addTextBox(String text, double x, double y, double width, double height);

	/**
	 * Return the name of the layout this slide is based on, or null
	 *
	 * @return
	 */
	public String getLayoutName();

	/**
	 * Return the shape of this slide (at any depth in its groups) with the supplied identifier, or null.<br>
	 * When several shapes wrongly share this identifier, the first one in z-order is returned.
	 *
	 * @param shapeId
	 * @return
	 */
	public PowerpointShape getShapeWithId(int shapeId);

	/**
	 * Return the speaker notes of this slide (the text of the body of its notes page), or null when it has none
	 *
	 * @return
	 */
	public String getNotes();

	/**
	 * Default base implementation for {@link PowerpointSlide}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointSlideImpl extends PowerpointObjectImpl implements PowerpointSlide {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointSlideImpl.class.getPackage().getName());

		@Override
		public String getNotes() {
			XSLFNotes notes = getSlide() != null ? getSlide().getNotes() : null;
			if (notes == null) {
				return null;
			}
			// The notes page also holds the image of the slide and the slide number: only keep its body
			StringBuilder returned = new StringBuilder();
			for (XSLFShape shape : notes.getShapes()) {
				if (shape instanceof XSLFTextShape && ((XSLFTextShape) shape).getTextType() == Placeholder.BODY) {
					String text = ((XSLFTextShape) shape).getText();
					if (text != null && !text.trim().isEmpty()) {
						returned.append(returned.length() > 0 ? "\n" : "").append(text.trim());
					}
				}
			}
			return returned.length() > 0 ? returned.toString() : null;
		}

		@Override
		public PowerpointShape getShapeWithId(int shapeId) {
			return findShapeWithId(getPowerpointShapes(), shapeId);
		}

		private static PowerpointShape findShapeWithId(List<PowerpointShape> shapes, int shapeId) {
			for (PowerpointShape shape : shapes) {
				if (shape.getShapeId() == shapeId) {
					return shape;
				}
				if (shape instanceof PowerpointShapeGroup) {
					PowerpointShape found = findShapeWithId(((PowerpointShapeGroup) shape).getChildren(), shapeId);
					if (found != null) {
						return found;
					}
				}
			}
			return null;
		}

		@Override
		public String getLayoutName() {
			return getSlide() != null && getSlide().getSlideLayout() != null ? getSlide().getSlideLayout().getName() : null;
		}

		@Override
		public PowerpointSlideshow getResourceData() {
			return getPowerpointSlideshow();
		}

		@Override
		public int getSlideNumber() {
			// XSLFSlide slide numbers are 1-based ; expose a 0-based index consistent with the shapes list
			return getSlide() != null ? getSlide().getSlideNumber() - 1 : -1;
		}

		@Override
		public long getSlideId() {
			if (getSlide() == null) {
				return -1;
			}
			// presentation.xml lists the slides as <p:sldId id="256" r:id="rId2"/>: match the relation of this slide
			XMLSlideShow slideShow = getSlide().getSlideShow();
			String relationId = slideShow.getRelationId(getSlide());
			CTSlideIdList slideIds = slideShow.getCTPresentation().getSldIdLst();
			if (relationId != null && slideIds != null) {
				for (CTSlideIdListEntry entry : slideIds.getSldIdArray()) {
					if (relationId.equals(entry.getId2())) {
						return entry.getId();
					}
				}
			}
			return -1;
		}

		@Override
		public String getTitle() {
			if (getSlide() == null) {
				return null;
			}
			for (XSLFTextShape placeholder : getSlide().getPlaceholders()) {
				Placeholder type = placeholder.getTextType();
				if (type == Placeholder.TITLE || type == Placeholder.CENTERED_TITLE) {
					return placeholder.getText();
				}
			}
			return null;
		}

		@Override
		public String getName() {
			String title = getTitle();
			if (title != null && title.length() > 0) {
				return title;
			}
			return "Slide" + (getSlideNumber() + 1);
		}

		@Override
		public String getUri() {
			String parentUri = getPowerpointSlideshow() != null ? getPowerpointSlideshow().getUri() : "pptx:slideshow";
			return parentUri + PowerpointObjectURI.PATH_SEPARATOR + PowerpointObjectURI.slideSegment(this);
		}

		@Override
		public PowerpointTextBox addTextBox(String text, double x, double y, double width, double height) {
			XSLFTextBox textBox = getSlide().createTextBox();
			textBox.setAnchor(new Rectangle2D.Double(x, y, width, height));
			textBox.setText(text);
			PowerpointTextBox powerpointTextBox = getFactory().makePowerpointTextBox();
			powerpointTextBox.setShape(textBox);
			addToPowerpointShapes(powerpointTextBox);
			powerpointTextBox.updateParagraphs(getFactory());
			return powerpointTextBox;
		}

	}

}
