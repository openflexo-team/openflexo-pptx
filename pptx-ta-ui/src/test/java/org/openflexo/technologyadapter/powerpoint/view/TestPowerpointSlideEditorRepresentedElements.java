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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openflexo.diana.Drawing.DrawingTreeNode;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;
import org.openflexo.test.UITest;

/**
 * An object selected elsewhere selects in the editor of a slide the elements standing for it, as given by
 * {@link PowerpointSlideEditor#setRepresentedElements(java.util.function.Function)}
 * 
 * @author sylvain
 * 
 */
public class TestPowerpointSlideEditorRepresentedElements extends OpenflexoTestCase {

	@Test
	@Category(UITest.class)
	public void testSelectingAnObjectSelectsItsElements() throws Exception {
		PowerpointSlideshow slideshow = getSlideshow();
		PowerpointSlide slide = slideshow.getPowerpointSlides().get(0);
		PowerpointShape first = slide.getPowerpointShapes().get(0);
		PowerpointShape second = slide.getPowerpointShapes().get(1);
		// The slideshow stands for an object of another module
		PowerpointSlideshowView view = new PowerpointSlideshowView(slideshow, null, null);
		PowerpointSlideEditor editor = view.getSlideView(slide).getEditor();

		editor.fireObjectSelected(slideshow);
		assertTrue("No mapping, nothing selected", editor.getSelectedObjects().isEmpty());

		view.setRepresentedElements(object -> object == slideshow ? Arrays.asList(first, second) : null);
		editor.fireObjectSelected(slideshow);
		assertEquals(new HashSet<>(Arrays.asList(first, second)), drawables(editor.getSelectedObjects()));

		editor.fireObjectDeselected(slideshow);
		assertTrue(editor.getSelectedObjects().isEmpty());

		// An element selected by itself is selected as before
		editor.fireObjectSelected(first);
		assertEquals(new HashSet<>(Arrays.asList(first)), drawables(editor.getSelectedObjects()));
		editor.fireResetSelection();
		assertTrue(editor.getSelectedObjects().isEmpty());

		view.deleteModuleView();
	}

	private static Set<Object> drawables(List<DrawingTreeNode<?, ?>> nodes) {
		Set<Object> returned = new HashSet<>();
		for (DrawingTreeNode<?, ?> node : nodes) {
			returned.add(node.getDrawable());
		}
		return returned;
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
