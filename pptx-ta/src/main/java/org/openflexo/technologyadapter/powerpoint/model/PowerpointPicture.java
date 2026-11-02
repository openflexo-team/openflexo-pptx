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

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Represents a PowerPoint picture (backed by a POI XSLFPictureShape).
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointPicture.PowerpointPictureImpl.class)
public interface PowerpointPicture extends PowerpointSimpleShape {

	/**
	 * Return the content type of the image (for instance image/png), or null
	 *
	 * @return
	 */
	public String getContentType();

	/**
	 * Return the name of the image file inside the presentation, or null
	 *
	 * @return
	 */
	public String getPictureFileName();

	/**
	 * Return the image itself, or null when its format cannot be read (vector formats such as EMF or SVG)
	 *
	 * @return
	 */
	public Image getImage();

	/**
	 * Default base implementation for {@link PowerpointPicture}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointPictureImpl extends PowerpointSimpleShapeImpl implements PowerpointPicture {

		private static final Logger logger = Logger.getLogger(PowerpointPictureImpl.class.getPackage().getName());

		private XSLFPictureData getPictureData() {
			return getShape() instanceof XSLFPictureShape ? ((XSLFPictureShape) getShape()).getPictureData() : null;
		}

		@Override
		public String getContentType() {
			return getPictureData() != null ? getPictureData().getContentType() : null;
		}

		@Override
		public String getPictureFileName() {
			return getPictureData() != null ? getPictureData().getFileName() : null;
		}

		@Override
		public Image getImage() {
			if (getPictureData() == null) {
				return null;
			}
			try {
				return ImageIO.read(new ByteArrayInputStream(getPictureData().getData()));
			} catch (IOException e) {
				logger.log(Level.FINE, "Could not read image " + getPictureFileName(), e);
				return null;
			}
		}

	}

}
