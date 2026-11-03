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
package org.openflexo.technologyadapter.powerpoint.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.junit.Test;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.technologyadapter.powerpoint.PowerpointTechnologyAdapter;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointChart;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointObject;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointObjectURI;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShapeGroup;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTable;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTableRow;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTextShape;
import org.openflexo.technologyadapter.powerpoint.rm.PowerpointSlideshowResource;

/**
 * PPTX-F-5: the actor references of a VirtualModelInstance pointing at the slides and shapes of an existing presentation are found again
 * after a reload, in a fresh service manager, and after the slides of the presentation were reordered.
 *
 * <p>
 * The presentation is <code>CycloCity-Company-Presentation.pptx</code>, copied into a resource center of its own with the instance. Its
 * slide 8 holds both cases that a positional URI or a plain shape id cannot designate reliably: shapes nested in groups, and two shapes
 * sharing id 25 (PPTX-D-1).
 *
 * @author sylvain
 *
 */
public class TestShapeActorReferenceReload extends OpenflexoTestCase {

	private static final Logger logger = Logger.getLogger(TestShapeActorReferenceReload.class.getPackage().getName());

	private static final String VIRTUAL_MODEL_URI = "http://openflexo.org/ppt-test/TestResourceCenter/FML/ShapeAnnotations.fml";
	private static final String DECK = "CycloCity-Company-Presentation.pptx";
	private static final String RC_URI = "http://openflexo.org/ppt-test/ShapeActorReferenceReload";
	/** Slide 8, 0-based */
	private static final int SLIDE_INDEX = 7;
	private static final int DUPLICATED_ID = 25;

	/** What identifies an annotated element independently of the objects of a load: label of the note -> description */
	private final Map<String, String> expectedActors = new LinkedHashMap<>();

	private File rcDirectory;
	private FlexoEditor editor;

	@Test
	public void testActorReferencesSurviveReloadAndSlideReordering() throws Exception {
		rcDirectory = Files.createTempDirectory("ShapeActorReferenceReload").toFile();
		// Deleted by tearDownClass() (and never by unloadServiceManager(), which only deletes it with a registered test resource center)
		testResourceCenterDirectory = rcDirectory;
		try (InputStream in = ResourceLocator.locateResource("TestResourceCenter/PPT/Business/" + DECK).openInputStream()) {
			Files.copy(in, new File(rcDirectory, DECK).toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		// 1. Annotate, save
		DirectoryResourceCenter rc = startServiceManager();
		PowerpointSlideshow slideshow = getSlideshow(rc);
		PowerpointSlide slide = slideshow.getPowerpointSlides().get(SLIDE_INDEX);

		PowerpointShape nested = firstNestedShape(slide.getPowerpointShapes());
		assertNotNull("No shape nested in a group on slide " + (SLIDE_INDEX + 1), nested);
		List<PowerpointShape> duplicates = new ArrayList<>();
		collectShapesWithId(slide.getPowerpointShapes(), DUPLICATED_ID, duplicates);
		assertEquals("Slide " + (SLIDE_INDEX + 1) + " should hold two shapes with id " + DUPLICATED_ID + " (PPTX-D-1)", 2,
				duplicates.size());
		assertNotEquals(duplicates.get(0).getUri(), duplicates.get(1).getUri());

		VirtualModel vm = serviceManager.getVirtualModelLibrary().getVirtualModel(VIRTUAL_MODEL_URI);
		assertNotNull(vm);
		FlexoConcept shapeNote = vm.getFlexoConcept("ShapeNote");
		FlexoConcept slideNote = vm.getFlexoConcept("SlideNote");
		assertEquals(PowerpointShape.class, shapeNote.getAccessibleProperty("shape").getResultingType());

		FMLRTVirtualModelInstance vmi = createVMI(rc, vm, slideshow.getResource());
		annotate(vmi, shapeNote, nested, "nested");
		annotate(vmi, shapeNote, duplicates.get(0), "duplicate1");
		annotate(vmi, shapeNote, duplicates.get(1), "duplicate2");
		annotate(vmi, slideNote, slide, "slide");
		assertActors(vmi, slideshow);
		String vmiURI = vmi.getResource().getURI();
		vmi.getResource().save();
		assertSerializedURIs();
		unloadServiceManager();

		// 2. Reload in a fresh service manager
		rc = startServiceManager();
		slideshow = getSlideshow(rc);
		vmi = loadVMI(vmiURI);
		assertActors(vmi, slideshow);
		assertEquals(SLIDE_INDEX, ((PowerpointSlide) actor(vmi, "slide")).getSlideNumber());
		unloadServiceManager();

		// 3. Move slide 8 to the front, outside Openflexo, then reload again
		File deck = new File(rcDirectory, DECK);
		try (FileInputStream in = new FileInputStream(deck); XMLSlideShow ppt = new XMLSlideShow(in)) {
			XSLFSlide moved = ppt.getSlides().get(SLIDE_INDEX);
			ppt.setSlideOrder(moved, 0);
			try (FileOutputStream out = new FileOutputStream(deck)) {
				ppt.write(out);
			}
		}
		rc = startServiceManager();
		slideshow = getSlideshow(rc);
		vmi = loadVMI(vmiURI);
		assertActors(vmi, slideshow);
		assertEquals("The annotated slide is now the first one", 0, ((PowerpointSlide) actor(vmi, "slide")).getSlideNumber());
		unloadServiceManager();
	}

	/**
	 * Every element of the sample decks - slides, shapes at any depth, rows, cells, paragraphs and series - is found again from its URI,
	 * including the shapes sharing an id with a sibling (PPTX-D-1)
	 */
	@Test
	public void testEveryElementResolvesFromItsURI() throws Exception {
		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);
		PowerpointTechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(PowerpointTechnologyAdapter.class);
		int decks = 0;
		int[] counts = new int[2];
		for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
			for (PowerpointSlideshowResource resource : ta.getPowerpointSlideShowRepository(rc).getAllResources()) {
				PowerpointSlideshow slideshow = resource.getResourceData();
				decks++;
				for (PowerpointSlide slide : slideshow.getPowerpointSlides()) {
					assertResolves(slideshow, slide, counts);
					for (PowerpointShape shape : slide.getPowerpointShapes()) {
						assertResolvesDeep(slideshow, shape, counts);
					}
				}
			}
		}
		logger.info(decks + " decks, " + counts[0] + " elements resolved from their URI, " + counts[1]
				+ " of them ranked among siblings sharing their id");
		assertTrue("Sample decks not found", decks >= 8);
		assertTrue("No shape shares its id with a sibling: the rank is not exercised", counts[1] > 0);
	}

	private static void assertResolvesDeep(PowerpointSlideshow slideshow, PowerpointShape shape, int[] counts) {
		assertResolves(slideshow, shape, counts);
		List<PowerpointObject> children = new ArrayList<>();
		if (shape instanceof PowerpointShapeGroup) {
			for (PowerpointShape child : ((PowerpointShapeGroup) shape).getChildren()) {
				assertResolvesDeep(slideshow, child, counts);
			}
		}
		if (shape instanceof PowerpointTextShape) {
			children.addAll(((PowerpointTextShape) shape).getParagraphs());
		}
		if (shape instanceof PowerpointTable) {
			for (PowerpointTableRow row : ((PowerpointTable) shape).getRows()) {
				children.add(row);
				children.addAll(row.getCells());
			}
		}
		if (shape instanceof PowerpointChart) {
			children.addAll(((PowerpointChart) shape).getSeries());
		}
		for (PowerpointObject child : children) {
			assertResolves(slideshow, child, counts);
		}
	}

	private static void assertResolves(PowerpointSlideshow slideshow, PowerpointObject element, int[] counts) {
		assertSame(element.getUri(), element, PowerpointObjectURI.resolve(slideshow, element.getUri()));
		counts[0]++;
		if (element.getUri().contains(",n=")) {
			counts[1]++;
		}
	}

	/** The instance stores the id-based URIs, not positional ones */
	private void assertSerializedURIs() throws Exception {
		StringBuilder serialized = new StringBuilder();
		try (Stream<Path> files = Files.walk(rcDirectory.toPath())) {
			for (Path file : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".fml.rt.xml"))::iterator) {
				serialized.append(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
			}
		}
		for (String label : expectedActors.keySet()) {
			logger.info("Note " + label + " -> " + expectedActors.get(label));
		}
		Matcher matcher = Pattern.compile("objectURI=\"([^\"]*)\"").matcher(serialized);
		int count = 0;
		while (matcher.find()) {
			String uri = URLDecoder.decode(matcher.group(1), "UTF-8");
			logger.info("Serialized " + uri);
			assertTrue(uri, uri.contains("#/slide[id="));
			count++;
		}
		assertEquals(expectedActors.size(), count);
	}

	private DirectoryResourceCenter startServiceManager() throws Exception {
		instanciateTestServiceManager(PowerpointTechnologyAdapter.class);
		editor = new DefaultFlexoEditor(null, serviceManager);
		DirectoryResourceCenter rc = DirectoryResourceCenter.instanciateNewDirectoryResourceCenter(rcDirectory,
				serviceManager.getResourceCenterService());
		rc.setDefaultBaseURI(RC_URI);
		serviceManager.getResourceCenterService().addToResourceCenters(rc);
		return rc;
	}

	private PowerpointSlideshow getSlideshow(DirectoryResourceCenter rc) throws Exception {
		PowerpointTechnologyAdapter ta = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(PowerpointTechnologyAdapter.class);
		for (PowerpointSlideshowResource resource : ta.getPowerpointSlideShowRepository(rc).getAllResources()) {
			if (resource.getName().startsWith("CycloCity")) {
				return resource.getResourceData();
			}
		}
		throw new AssertionError(DECK + " not found in " + rcDirectory);
	}

	private FMLRTVirtualModelInstance createVMI(DirectoryResourceCenter rc, VirtualModel vm, PowerpointSlideshowResource deck) {
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
				.makeNewAction(rc.getVirtualModelInstanceRepository().getRootFolder(), null, editor);
		action.setNewVirtualModelInstanceName("Annotations");
		action.setNewVirtualModelInstanceTitle("Annotations");
		action.setVirtualModel(vm);
		CreationScheme cs = vm.getCreationSchemes().get(0);
		action.setCreationScheme(cs);
		action.setParameterValue(cs.getParameters().get(0), deck);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		return action.getNewVirtualModelInstance();
	}

	private FMLRTVirtualModelInstance loadVMI(String vmiURI) throws Exception {
		FMLRTVirtualModelInstanceResource resource = (FMLRTVirtualModelInstanceResource) serviceManager.getResourceManager()
				.getResource(vmiURI);
		assertNotNull("Instance " + vmiURI + " not found after reload", resource);
		return resource.getResourceData();
	}

	private void annotate(FMLRTVirtualModelInstance vmi, FlexoConcept concept, PowerpointObject element, String label) {
		CreateFlexoConceptInstance action = CreateFlexoConceptInstance.actionType.makeNewAction(vmi, null, editor);
		action.setFlexoConcept(concept);
		CreationScheme cs = concept.getCreationSchemes().get(0);
		action.setCreationScheme(cs);
		action.setParameterValue(cs.getParameters().get(0), element);
		action.setParameterValue(cs.getParameters().get(1), label);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		expectedActors.put(label, describe(element));
	}

	/**
	 * Every note points at the element it was created on - same slide, same shape, same position in the shape tree - and at an object of
	 * the presentation currently loaded
	 */
	private void assertActors(FMLRTVirtualModelInstance vmi, PowerpointSlideshow slideshow) {
		assertEquals(expectedActors.size(), vmi.getFlexoConceptInstances().size());
		for (Map.Entry<String, String> expected : expectedActors.entrySet()) {
			PowerpointObject actor = actor(vmi, expected.getKey());
			assertNotNull("Actor of note " + expected.getKey() + " not found", actor);
			assertSame(slideshow, actor.getResourceData());
			assertEquals("Actor of note " + expected.getKey(), expected.getValue(), describe(actor));
		}
	}

	private static PowerpointObject actor(FMLRTVirtualModelInstance vmi, String label) {
		for (FlexoConceptInstance fci : vmi.getFlexoConceptInstances()) {
			if (label.equals(fci.getFlexoPropertyValue(fci.getFlexoConcept().getAccessibleProperty("label")))) {
				String role = fci.getFlexoConcept().getName().equals("ShapeNote") ? "shape" : "slide";
				return fci.getFlexoActor(role);
			}
		}
		throw new AssertionError("No note " + label);
	}

	/** Describes an element by what does not depend on the load nor on the order of the slides */
	private static String describe(PowerpointObject element) {
		if (element instanceof PowerpointSlide) {
			PowerpointSlide slide = (PowerpointSlide) element;
			return "slide " + slide.getSlideId() + " '" + slide.getTitle() + "' " + slide.getPowerpointShapes().size() + " shapes";
		}
		PowerpointShape shape = (PowerpointShape) element;
		String path = "";
		for (PowerpointShape s = shape; s != null; s = s.getContainer()) {
			List<PowerpointShape> siblings = s.getContainer() != null ? s.getContainer().getChildren()
					: s.getPowerpointSlide().getPowerpointShapes();
			path = "/" + siblings.indexOf(s) + path;
		}
		return describe(shape.getPowerpointSlide()) + " > shape " + shape.getShapeId() + " '" + shape.getName() + "' at " + path + " "
				+ shape.getImplementedInterface().getSimpleName();
	}

	private static PowerpointShape firstNestedShape(List<PowerpointShape> shapes) {
		for (PowerpointShape shape : shapes) {
			if (shape instanceof PowerpointShapeGroup && !((PowerpointShapeGroup) shape).getChildren().isEmpty()) {
				return ((PowerpointShapeGroup) shape).getChildren().get(0);
			}
		}
		return null;
	}

	private static void collectShapesWithId(List<PowerpointShape> shapes, int id, List<PowerpointShape> found) {
		for (PowerpointShape shape : shapes) {
			if (shape.getShapeId() == id) {
				found.add(shape);
			}
			if (shape instanceof PowerpointShapeGroup) {
				collectShapesWithId(((PowerpointShapeGroup) shape).getChildren(), id, found);
			}
		}
	}

}
