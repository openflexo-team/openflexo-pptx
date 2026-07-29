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

import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;

/**
 * Represents a PowerPoint presentation, as a PAMELA entity wrapping the POI {@link XMLSlideShow} concept.
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointSlideshow.PowerpointSlideshowImpl.class)
@Imports({ @Import(PowerpointTextBox.class), @Import(PowerpointAutoShape.class), @Import(PowerpointPicture.class),
		@Import(PowerpointLine.class), @Import(PowerpointShapeGroup.class) })
public interface PowerpointSlideshow extends PowerpointObject, ResourceData<PowerpointSlideshow> {

	@PropertyIdentifier(type = XMLSlideShow.class)
	public static final String SLIDE_SHOW_KEY = "slideShow";
	@PropertyIdentifier(type = PowerpointSlide.class, cardinality = Cardinality.LIST)
	public static final String POWERPOINT_SLIDES_KEY = "powerpointSlides";

	/**
	 * Return the POI {@link XMLSlideShow} wrapped by this {@link PowerpointSlideshow}
	 *
	 * @return
	 */
	@Getter(value = SLIDE_SHOW_KEY, ignoreType = true)
	public XMLSlideShow getSlideShow();

	/**
	 * Sets the POI {@link XMLSlideShow} wrapped by this {@link PowerpointSlideshow}
	 *
	 * @param slideShow
	 */
	@Setter(SLIDE_SHOW_KEY)
	public void setSlideShow(XMLSlideShow slideShow);

	/**
	 * Return all {@link PowerpointSlide} defined in this {@link PowerpointSlideshow}
	 *
	 * @return
	 */
	@Getter(value = POWERPOINT_SLIDES_KEY, cardinality = Cardinality.LIST, inverse = PowerpointSlide.SLIDE_SHOW_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<PowerpointSlide> getPowerpointSlides();

	@Setter(POWERPOINT_SLIDES_KEY)
	public void setPowerpointSlides(List<PowerpointSlide> slides);

	@Adder(POWERPOINT_SLIDES_KEY)
	@PastingPoint
	public void addToPowerpointSlides(PowerpointSlide aSlide);

	@Remover(POWERPOINT_SLIDES_KEY)
	public void removeFromPowerpointSlides(PowerpointSlide aSlide);

	@Override
	public PowerpointSlideshowResource getResource();

	/**
	 * Create a new empty slide, append it to this presentation and return the matching {@link PowerpointSlide}
	 *
	 * @return the newly created {@link PowerpointSlide}
	 */
	public PowerpointSlide addSlide();

	/**
	 * Return the {@link PowerpointSlide} wrapping supplied POI {@link XSLFSlide}, or null
	 *
	 * @param slide
	 * @return
	 */
	public PowerpointSlide getSlideFromXSLFSlide(XSLFSlide slide);

	/**
	 * Default base implementation for {@link PowerpointSlideshow}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointSlideshowImpl extends PowerpointObjectImpl implements PowerpointSlideshow {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointSlideshowImpl.class.getPackage().getName());

		@Override
		public PowerpointSlideshow getResourceData() {
			return this;
		}

		@Override
		public String getName() {
			return getResource() != null ? getResource().getName() : null;
		}

		@Override
		public String getUri() {
			return getResource() != null ? getResource().getURI() : "pptx:slideshow";
		}

		@Override
		public PowerpointSlide addSlide() {
			XSLFSlide xslfSlide = getSlideShow().createSlide();
			PowerpointSlide slide = getFactory().makePowerpointSlide();
			slide.setSlide(xslfSlide);
			addToPowerpointSlides(slide);
			return slide;
		}

		@Override
		public PowerpointSlide getSlideFromXSLFSlide(XSLFSlide slide) {
			for (PowerpointSlide powerpointSlide : getPowerpointSlides()) {
				if (powerpointSlide.getSlide() == slide) {
					return powerpointSlide;
				}
			}
			return null;
		}

	}

}
