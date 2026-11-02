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

import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openxmlformats.schemas.drawingml.x2006.main.CTConnection;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualConnectorProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;

/**
 * Represents a PowerPoint line / connector (backed by a POI XSLFConnectorShape).
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointLine.PowerpointLineImpl.class)
public interface PowerpointLine extends PowerpointSimpleShape {

	/**
	 * Return the decoration at the start of the line (for instance TRIANGLE), or null when there is none
	 *
	 * @return
	 */
	public String getHeadDecoration();

	/**
	 * Return the decoration at the end of the line (for instance TRIANGLE), or null when there is none
	 *
	 * @return
	 */
	public String getTailDecoration();

	/**
	 * Return the shape the start of this connector is glued to, or null
	 *
	 * @return
	 */
	public PowerpointShape getStartShape();

	/**
	 * Return the shape the end of this connector is glued to, or null
	 *
	 * @return
	 */
	public PowerpointShape getEndShape();

	/**
	 * Return the index of the connection site of {@link #getStartShape()} the connector is glued to, or -1
	 *
	 * @return
	 */
	public int getStartConnectionSite();

	/**
	 * Return the index of the connection site of {@link #getEndShape()} the connector is glued to, or -1
	 *
	 * @return
	 */
	public int getEndConnectionSite();

	/**
	 * Default base implementation for {@link PowerpointLine}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointLineImpl extends PowerpointSimpleShapeImpl implements PowerpointLine {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointLineImpl.class.getPackage().getName());

		private CTNonVisualConnectorProperties getConnectorProperties() {
			if (getShape() != null && getShape().getXmlObject() instanceof CTConnector) {
				CTConnector connector = (CTConnector) getShape().getXmlObject();
				if (connector.getNvCxnSpPr() != null) {
					return connector.getNvCxnSpPr().getCNvCxnSpPr();
				}
			}
			return null;
		}

		private CTConnection getStartConnection() {
			CTNonVisualConnectorProperties properties = getConnectorProperties();
			return properties != null && properties.isSetStCxn() ? properties.getStCxn() : null;
		}

		private CTConnection getEndConnection() {
			CTNonVisualConnectorProperties properties = getConnectorProperties();
			return properties != null && properties.isSetEndCxn() ? properties.getEndCxn() : null;
		}

		private PowerpointShape getConnectedShape(CTConnection connection) {
			if (connection == null || getPowerpointSlide() == null) {
				return null;
			}
			return getPowerpointSlide().getShapeWithId((int) connection.getId());
		}

		@Override
		public PowerpointShape getStartShape() {
			return getConnectedShape(getStartConnection());
		}

		@Override
		public PowerpointShape getEndShape() {
			return getConnectedShape(getEndConnection());
		}

		@Override
		public int getStartConnectionSite() {
			return getStartConnection() != null ? (int) getStartConnection().getIdx() : -1;
		}

		@Override
		public int getEndConnectionSite() {
			return getEndConnection() != null ? (int) getEndConnection().getIdx() : -1;
		}

		@Override
		public String getHeadDecoration() {
			if (getShape() instanceof XSLFSimpleShape && ((XSLFSimpleShape) getShape()).getLineHeadDecoration() != null) {
				return ((XSLFSimpleShape) getShape()).getLineHeadDecoration().toString();
			}
			return null;
		}

		@Override
		public String getTailDecoration() {
			if (getShape() instanceof XSLFSimpleShape && ((XSLFSimpleShape) getShape()).getLineTailDecoration() != null) {
				return ((XSLFSimpleShape) getShape()).getLineTailDecoration().toString();
			}
			return null;
		}

	}

}
