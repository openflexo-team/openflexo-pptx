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

package org.openflexo.technologyadapter.powerpoint.model.io;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFConnectorShape;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointModelFactory;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointObject;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;

/**
 * Converts a POI {@link XMLSlideShow} (OOXML / XSLF) into the OpenFlexo PowerPoint PAMELA model.<br>
 * The whole slide/shape tree is built eagerly, backed by the corresponding POI XSLF objects.
 *
 * @author sylvain
 *
 */
public class BasicPowerpointModelConverter {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BasicPowerpointModelConverter.class.getPackage().getName());

	private final PowerpointSlideshowResource resource;

	/** Correspondence between POI XSLF objects and their OpenFlexo counterparts. */
	private final Map<Object, PowerpointObject> powerpointObjects = new HashMap<>();

	public BasicPowerpointModelConverter(PowerpointSlideshowResource resource) {
		this.resource = resource;
	}

	private PowerpointModelFactory getFactory() {
		return resource.getFactory();
	}

	/**
	 * Convert a POI {@link XMLSlideShow} into a {@link PowerpointSlideshow}
	 */
	public PowerpointSlideshow convertPowerpointSlideshow(XMLSlideShow slideShow) {
		PowerpointSlideshow powerpointSlideshow = getFactory().makePowerpointSlideshow();
		powerpointSlideshow.setSlideShow(slideShow);
		powerpointSlideshow.setResource(resource);
		powerpointObjects.put(slideShow, powerpointSlideshow);
		for (XSLFSlide slide : slideShow.getSlides()) {
			PowerpointSlide powerpointSlide = convertSlide(slide, powerpointSlideshow);
			powerpointSlideshow.addToPowerpointSlides(powerpointSlide);
		}
		return powerpointSlideshow;
	}

	private PowerpointSlide convertSlide(XSLFSlide slide, PowerpointSlideshow slideshow) {
		PowerpointSlide powerpointSlide = (PowerpointSlide) powerpointObjects.get(slide);
		if (powerpointSlide == null) {
			powerpointSlide = getFactory().makePowerpointSlide();
			powerpointSlide.setSlide(slide);
			powerpointObjects.put(slide, powerpointSlide);
			for (XSLFShape shape : slide.getShapes()) {
				PowerpointShape powerpointShape = convertShape(shape, powerpointSlide);
				if (powerpointShape != null) {
					powerpointSlide.addToPowerpointShapes(powerpointShape);
				}
			}
		}
		return powerpointSlide;
	}

	private PowerpointShape convertShape(XSLFShape shape, PowerpointSlide slide) {
		PowerpointShape powerpointShape = (PowerpointShape) powerpointObjects.get(shape);
		if (powerpointShape != null) {
			return powerpointShape;
		}
		// Order matters: most specific POI types first
		if (shape instanceof XSLFGroupShape) {
			powerpointShape = getFactory().makePowerpointShapeGroup();
		}
		else if (shape instanceof XSLFPictureShape) {
			powerpointShape = getFactory().makePowerpointPicture();
		}
		else if (shape instanceof XSLFConnectorShape) {
			powerpointShape = getFactory().makePowerpointLine();
		}
		else if (shape instanceof XSLFTextBox) {
			powerpointShape = getFactory().makePowerpointTextBox();
		}
		else if (shape instanceof XSLFAutoShape) {
			powerpointShape = getFactory().makePowerpointAutoShape();
		}
		else {
			logger.fine("Ignoring unsupported PowerPoint shape: " + shape.getClass().getName());
			return null;
		}
		powerpointShape.setShape(shape);
		powerpointObjects.put(shape, powerpointShape);
		return powerpointShape;
	}

	/**
	 * Return the correspondence map between POI XSLF objects and their OpenFlexo counterparts.
	 *
	 * @return
	 */
	public Map<Object, PowerpointObject> getPowerpointObjects() {
		return powerpointObjects;
	}

	public void delete() {
		powerpointObjects.clear();
	}

}
