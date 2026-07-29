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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTextBox;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;

/**
 * Integration test proving the symmetry of the PowerPoint resource: an empty .pptx presentation is
 * created from scratch, populated (slide + text box), saved to disk, then reloaded from disk with
 * every change persisted.
 *
 * <p>
 * This complements the FML-script use cases (which drive the model in memory) by exercising the
 * {@code save} / {@code load} round-trip that FML-script cannot yet express (no save directive).
 *
 * @author sylvain
 *
 */
public class PowerpointResourceRoundTripTest extends OpenflexoTestCase {

	@Test
	public void testCreatePopulateSaveReload() throws Exception {

		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);

		PowerpointTechnologyAdapter ta = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(PowerpointTechnologyAdapter.class);
		assertNotNull(ta);

		DirectoryResourceCenter rc = makeNewDirectoryResourceCenter();

		// Create a brand-new (empty) .pptx resource
		File file = new File(rc.getRootDirectory(), "RoundTrip.pptx");
		PowerpointSlideshowResource resource = ta.getPowerpointSlideshowResourceFactory().makeResource(file, rc, true);
		assertNotNull(resource);

		PowerpointSlideshow slideshow = resource.getResourceData();
		assertNotNull(slideshow);
		assertEquals(0, slideshow.getPowerpointSlides().size());

		// Populate: one slide carrying one text box
		PowerpointSlide slide = slideshow.addSlide();
		assertEquals(1, slideshow.getPowerpointSlides().size());
		PowerpointTextBox box = slide.addTextBox("Persisted content", 40, 40, 300, 50);
		assertEquals("Persisted content", box.getText());
		assertEquals(1, slide.getPowerpointShapes().size());

		// Save to disk
		resource.save();

		// Drop the in-memory model and reload strictly from the file
		resource.unloadResourceData(false);
		PowerpointSlideshow reloaded = resource.loadResourceData();

		// Assert every change survived the round-trip
		assertNotNull(reloaded);
		assertEquals(1, reloaded.getPowerpointSlides().size());
		PowerpointSlide reloadedSlide = reloaded.getPowerpointSlides().get(0);
		assertEquals(1, reloadedSlide.getPowerpointShapes().size());
		assertEquals("Persisted content", reloadedSlide.getPowerpointShapes().get(0).getText());
	}

}
