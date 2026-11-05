/**
 *
 * Copyright (c) 2026, Openflexo
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

package org.openflexo.technologyadapter.powerpoint.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;

/**
 * The thumbnails of the slides as a column (their width follows the width of the panel) or as a row (their height follows the height of
 * the panel)
 * 
 * @author sylvain
 * 
 */
public class TestPowerpointSlideThumbnailsPanel extends OpenflexoTestCase {

	@Test
	public void testColumnFollowsWidth() throws Exception {
		PowerpointSlideThumbnailsPanel panel = new PowerpointSlideThumbnailsPanel(getSlideshow(), slide -> {});
		assertFalse(panel.isHorizontal());
		panel.setSize(300, 800);
		panel.doLayout();
		panel.adjustThumbnailWidth();
		int width = panel.getThumbnailWidth();
		panel.setSize(200, 800);
		panel.doLayout();
		panel.adjustThumbnailWidth();
		assertEquals(width - 100, panel.getThumbnailWidth());
	}

	@Test
	public void testRowFollowsHeight() throws Exception {
		PowerpointSlideshow slideshow = getSlideshow();
		PowerpointSlideThumbnailsPanel panel = new PowerpointSlideThumbnailsPanel(slideshow, slide -> {}, true);
		assertTrue(panel.isHorizontal());
		double ratio = slideshow.getPageWidth() / slideshow.getPageHeight();
		panel.setSize(800, 150);
		panel.doLayout();
		panel.adjustThumbnailWidth();
		int width = panel.getThumbnailWidth();
		panel.setSize(800, 250);
		panel.doLayout();
		panel.adjustThumbnailWidth();
		// 100 pixels more in height give the thumbnails 100 pixels more in height, whatever the width of the panel
		assertEquals(width + 100 * ratio, panel.getThumbnailWidth(), 1.0);
		panel.setSize(400, 250);
		panel.doLayout();
		panel.adjustThumbnailWidth();
		assertEquals(width + 100 * ratio, panel.getThumbnailWidth(), 1.0);
	}

	private static PowerpointSlideshow getSlideshow() throws Exception {
		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);
		PowerpointTechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(PowerpointTechnologyAdapter.class);
		for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
			for (PowerpointSlideshowResource resource : ta.getPowerpointSlideShowRepository(rc).getAllResources()) {
				if (resource.getName().startsWith("CycloCity")) {
					return resource.getResourceData();
				}
			}
		}
		throw new AssertionError("CycloCity-Company-Presentation.pptx not found");
	}

}
