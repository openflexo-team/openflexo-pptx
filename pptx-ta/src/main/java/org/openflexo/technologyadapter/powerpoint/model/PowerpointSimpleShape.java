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
import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Represents a PowerPoint simple shape (a non-group shape backed by a POI XSLFSimpleShape).
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
public interface PowerpointSimpleShape extends PowerpointShape {

	/**
	 * Return the name of the geometry of this shape (a preset such as RECT or ROUND_RECT), or null for a custom geometry
	 *
	 * @return
	 */
	public String getShapeTypeName();

	/**
	 * Return the rotation of this shape, in degrees
	 *
	 * @return
	 */
	public double getRotation();

	/**
	 * Return the solid fill color of this shape, or null when it has none
	 *
	 * @return
	 */
	public Color getFillColor();

	/**
	 * Return the solid line color of this shape, or null when it has none
	 *
	 * @return
	 */
	public Color getLineColor();

	/**
	 * Return the width of the line of this shape, in points
	 *
	 * @return
	 */
	public double getLineWidth();

	/**
	 * Default base implementation for {@link PowerpointSimpleShape}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointSimpleShapeImpl extends PowerpointShapeImpl implements PowerpointSimpleShape {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointSimpleShapeImpl.class.getPackage().getName());

		private XSLFSimpleShape getSimpleShape() {
			return getShape() instanceof XSLFSimpleShape ? (XSLFSimpleShape) getShape() : null;
		}

		@Override
		public String getShapeTypeName() {
			return getSimpleShape() != null && getSimpleShape().getShapeType() != null ? getSimpleShape().getShapeType().toString() : null;
		}

		@Override
		public double getRotation() {
			return getSimpleShape() != null ? getSimpleShape().getRotation() : 0;
		}

		@Override
		public Color getFillColor() {
			return getSimpleShape() != null ? getSimpleShape().getFillColor() : null;
		}

		@Override
		public Color getLineColor() {
			return getSimpleShape() != null ? getSimpleShape().getLineColor() : null;
		}

		@Override
		public double getLineWidth() {
			return getSimpleShape() != null ? getSimpleShape().getLineWidth() : 0;
		}

	}

}
