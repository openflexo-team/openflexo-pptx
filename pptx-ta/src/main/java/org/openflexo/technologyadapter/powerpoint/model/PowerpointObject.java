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

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;

/**
 * Common API for all objects involved in the PowerPoint model.<br>
 * Every PowerPoint model object is a PAMELA entity wrapping an underlying Apache POI XSLF object.
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
public interface PowerpointObject extends FlexoObject, InnerResourceData<PowerpointSlideshow>,
		TechnologyObject<PowerpointTechnologyAdapter> {

	/**
	 * Return a displayable name for this object
	 *
	 * @return
	 */
	public String getName();

	/**
	 * Return a hierarchical URI identifying this object within its enclosing presentation.<br>
	 * URIs are stable within a load session (used by the model slot to resolve actor references).
	 *
	 * @return
	 */
	public String getUri();

	/**
	 * Return the {@link PowerpointModelFactory} used to build this object
	 *
	 * @return
	 */
	public PowerpointModelFactory getFactory();

	/**
	 * Default base implementation for {@link PowerpointObject}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointObjectImpl extends FlexoObjectImpl implements PowerpointObject {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointObjectImpl.class.getPackage().getName());

		@Override
		public PowerpointTechnologyAdapter getTechnologyAdapter() {
			if (getResourceData() != null && getResourceData().getResource() != null) {
				return getResourceData().getResource().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public PowerpointModelFactory getFactory() {
			if (getResourceData() != null && getResourceData().getResource() != null) {
				return getResourceData().getResource().getFactory();
			}
			return null;
		}

		@Override
		public LocalizedDelegate getLocales() {
			if (getTechnologyAdapter() != null) {
				return getTechnologyAdapter().getLocales();
			}
			return super.getLocales();
		}

		@Override
		public String toString() {
			return getImplementedInterface().getSimpleName() + "-" + getName();
		}

	}
}
