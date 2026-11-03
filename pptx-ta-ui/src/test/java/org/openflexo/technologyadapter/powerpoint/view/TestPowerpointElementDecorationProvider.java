/**
 *
 * Copyright (c) 2014-2026, Openflexo
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openflexo.diana.ColorBackgroundStyle;
import org.openflexo.diana.NoneBackgroundStyle;
import org.openflexo.diana.ShapeGraphicalRepresentation;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShapeGroup;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideElement;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;
import org.openflexo.test.UITest;

/**
 * PPTX-F-6: a {@link PowerpointElementDecorationProvider} changes the graphical representation of the elements it decorates, follows its
 * refreshes, and leaves the other elements transparent.
 * 
 * @author sylvain
 * 
 */
public class TestPowerpointElementDecorationProvider extends OpenflexoTestCase {

	private static final Color BORDER = Color.RED;
	private static final Color TINT = new Color(0, 0, 255, 64);

	/**
	 * Decorates a single element, with a badge that the test changes
	 */
	private static class SingleElementDecorations extends PowerpointElementDecorationProvider {
		private PowerpointSlideElement decorated;
		private String badge;

		@Override
		public Decoration getDecoration(PowerpointSlideElement element) {
			return element == decorated ? new Decoration(BORDER, TINT, badge) : null;
		}
	}

	@Test
	public void testDecorationChangesGraphicalRepresentation() throws Exception {
		PowerpointSlide slide = getSlide();
		PowerpointShape decorated = nestedShape(slide);
		PowerpointShape other = slide.getPowerpointShapes().get(0);

		PowerpointSlideDrawing drawing = new PowerpointSlideDrawing(slide);
		drawing.updateGraphicalObjectsHierarchy();
		ShapeGraphicalRepresentation gr = getGR(drawing, decorated);
		ShapeGraphicalRepresentation otherGR = getGR(drawing, other);
		assertUndecorated(gr);

		SingleElementDecorations provider = new SingleElementDecorations();
		provider.decorated = decorated;
		provider.badge = "A";
		drawing.setDecorationProvider(provider);
		assertSame("The graphical representation is decorated, not replaced", gr, getGR(drawing, decorated));
		assertDecorated(gr, "A");
		assertUndecorated(otherGR);

		provider.badge = "B";
		provider.refresh();
		assertDecorated(gr, "B");

		provider.decorated = other;
		provider.refresh();
		assertUndecorated(gr);
		assertDecorated(otherGR, "B");

		drawing.setDecorationProvider(null);
		assertUndecorated(otherGR);
		provider.decorated = decorated;
		provider.refresh();
		assertUndecorated(gr);
		drawing.delete();
	}

	/**
	 * The provider installed on the slideshow view reaches the drawing of every slide
	 */
	@Test
	@Category(UITest.class)
	public void testDecorationInstalledOnSlideshowView() throws Exception {
		PowerpointSlide slide = getSlide();
		PowerpointShape decorated = nestedShape(slide);
		PowerpointSlideshowView view = new PowerpointSlideshowView(slide.getPowerpointSlideshow(), null, null);
		SingleElementDecorations provider = new SingleElementDecorations();
		provider.decorated = decorated;
		provider.badge = "A";
		view.setDecorationProvider(provider);
		PowerpointSlideDrawing drawing = view.getSlideView(slide).getEditor().getDrawing();
		assertSame(provider, drawing.getDecorationProvider());
		drawing.updateGraphicalObjectsHierarchy();
		assertDecorated(getGR(drawing, decorated), "A");
		view.deleteModuleView();
		assertNull(drawing.getDecorationProvider());
	}

	private static PowerpointSlide getSlide() throws Exception {
		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);
		PowerpointTechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(PowerpointTechnologyAdapter.class);
		for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
			for (PowerpointSlideshowResource resource : ta.getPowerpointSlideShowRepository(rc).getAllResources()) {
				if (resource.getName().startsWith("CycloCity")) {
					PowerpointSlideshow slideshow = resource.getResourceData();
					// Slide 8 holds groups
					return slideshow.getPowerpointSlides().get(7);
				}
			}
		}
		throw new AssertionError("CycloCity-Company-Presentation.pptx not found");
	}

	private static PowerpointShape nestedShape(PowerpointSlide slide) {
		for (PowerpointShape shape : slide.getPowerpointShapes()) {
			if (shape instanceof PowerpointShapeGroup && !((PowerpointShapeGroup) shape).getChildren().isEmpty()) {
				return ((PowerpointShapeGroup) shape).getChildren().get(0);
			}
		}
		throw new AssertionError("No group on " + slide);
	}

	private static ShapeGraphicalRepresentation getGR(PowerpointSlideDrawing drawing, PowerpointSlideElement element) {
		assertNotNull("No node for " + element, drawing.getDrawingTreeNode(element));
		return (ShapeGraphicalRepresentation) drawing.getDrawingTreeNode(element).getGraphicalRepresentation();
	}

	private static void assertDecorated(ShapeGraphicalRepresentation gr, String badge) {
		assertEquals(BORDER, gr.getForeground().getColor());
		assertTrue(gr.getBackground() instanceof ColorBackgroundStyle);
		assertEquals(new Color(0, 0, 255), ((ColorBackgroundStyle) gr.getBackground()).getColor());
		assertTrue(gr.getBackground().getUseTransparency());
		assertEquals(64 / 255f, gr.getBackground().getTransparencyLevel(), 0.001);
		assertEquals(badge, gr.getText());
	}

	private static void assertUndecorated(ShapeGraphicalRepresentation gr) {
		assertTrue(gr.getForeground().getNoStroke());
		assertTrue(gr.getBackground() instanceof NoneBackgroundStyle);
		assertNull(gr.getText());
	}

}
