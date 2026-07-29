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

package org.openflexo.technologyadapter.powerpoint;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.annotations.DeclareModelSlots;
import org.openflexo.foundation.fml.annotations.DeclareResourceFactories;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterBindingFactory;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterInitializationException;
import org.openflexo.technologyadapter.powerpoint.fml.binding.PowerpointBindingFactory;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideShowRepository;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResourceFactory;

/**
 * This class defines and implements the PowerPoint technology adapter.<br>
 * It bridges FML to real {@code .pptx} (OOXML) presentations, read and written through Apache POI XSLF.
 *
 * @author sylvain, vincent, Christophe
 *
 */
@DeclareModelSlots({ BasicPowerpointModelSlot.class })
@DeclareResourceFactories({ PowerpointSlideshowResourceFactory.class })
public class PowerpointTechnologyAdapter extends TechnologyAdapter<PowerpointTechnologyAdapter> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PowerpointTechnologyAdapter.class.getPackage().getName());

	private static final PowerpointBindingFactory BINDING_FACTORY = new PowerpointBindingFactory();

	/**
	 * Constructor.
	 *
	 * @throws TechnologyAdapterInitializationException
	 */
	public PowerpointTechnologyAdapter() throws TechnologyAdapterInitializationException {
	}

	@Override
	public String getName() {
		return "Powerpoint technology adapter";
	}

	@Override
	protected String getLocalizationDirectory() {
		return "FlexoLocalization/PowerPointTechnologyAdapter";
	}

	@Override
	public TechnologyAdapterBindingFactory getTechnologyAdapterBindingFactory() {
		return BINDING_FACTORY;
	}

	@Override
	public <I> boolean isIgnorable(FlexoResourceCenter<I> resourceCenter, I contents) {
		return false;
	}

	public <I> PowerpointSlideShowRepository<I> getPowerpointSlideShowRepository(FlexoResourceCenter<I> resourceCenter) {
		PowerpointSlideShowRepository<I> returned = resourceCenter.retrieveRepository(PowerpointSlideShowRepository.class, this);
		if (returned == null) {
			returned = PowerpointSlideShowRepository.instanciateNewRepository(this, resourceCenter);
			resourceCenter.registerRepository(returned, PowerpointSlideShowRepository.class, this);
		}
		return returned;
	}

	@Override
	public String getIdentifier() {
		return "PPT";
	}

	public PowerpointSlideshowResourceFactory getPowerpointSlideshowResourceFactory() {
		return getResourceFactory(PowerpointSlideshowResourceFactory.class);
	}

}
