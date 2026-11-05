/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Openflexo-technology-adapters-ui, a component of the software infrastructure 
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import javax.swing.JPanel;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.selection.SelectionListener;
import org.openflexo.selection.SelectionManager;
import org.openflexo.swing.layout.JXMultiSplitPane;
import org.openflexo.swing.layout.KnobDividerPainter;
import org.openflexo.swing.layout.MultiSplitLayout;
import org.openflexo.swing.layout.MultiSplitLayout.Leaf;
import org.openflexo.swing.layout.MultiSplitLayout.Split;
import org.openflexo.swing.layout.MultiSplitLayoutFactory;
import org.openflexo.technologyadapter.powerpoint.gui.PowerpointSlideBrowser;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideElement;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.view.SelectionSynchronizedModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * Module view of a {@link PowerpointSlideshow}: the shown slide in the center, zoomed to fit the available space, and the thumbnails of
 * all the slides to choose it, in a split pane. The thumbnails are a column on the right by default, or a row at the top or at the bottom
 * ({@link ThumbnailsPosition}). A browser of the contents of the shown slide is put in the left side of the
 * perspective.
 * <p>
 * The view can be embedded by another module: it only relies on the {@link FlexoController} it is given (its selection manager, and the
 * removal of the module view), and a {@link PowerpointElementDecorationProvider} installed with
 * {@link #setDecorationProvider(PowerpointElementDecorationProvider)} paints that module's information over the elements of the slides.
 * Such a module may place the browser of the shown slide ({@link #getSlideBrowser()}) itself, after {@link #setInstallsBrowser(boolean)}
 * with false.
 * 
 * @author vincent, sylvain
 * 
 */
@SuppressWarnings("serial")
public class PowerpointSlideshowView extends JPanel implements SelectionSynchronizedModuleView<PowerpointSlideshow> {

	/**
	 * Where the thumbnails of the slides are placed around the shown slide
	 */
	public enum ThumbnailsPosition {
		RIGHT, TOP, BOTTOM
	}

	private final PowerpointSlideshow slideshow;
	private final FlexoPerspective declaredPerspective;
	private final List<PowerpointSlideView> slideViews = new ArrayList<>();
	private static final String SLIDE_LEAF = "slide";
	private static final String THUMBNAILS_LEAF = "thumbnails";

	private final PowerpointSlideBrowser browser;
	private final CardLayout slidesLayout = new CardLayout();
	private final JPanel slidesPanel = new JPanel(slidesLayout);
	private final PowerpointSlideThumbnailsPanel thumbnailsPanel;
	private PowerpointSlideView shownSlideView;

	private final FlexoController controller;

	private PowerpointElementDecorationProvider decorationProvider;

	private boolean installsBrowser = true;

	private final ThumbnailsPosition thumbnailsPosition;

	public PowerpointSlideshowView(PowerpointSlideshow slideshow, FlexoController controller, FlexoPerspective perspective) {
		this(slideshow, controller, perspective, ThumbnailsPosition.RIGHT);
	}

	public PowerpointSlideshowView(PowerpointSlideshow slideshow, FlexoController controller, FlexoPerspective perspective,
			ThumbnailsPosition thumbnailsPosition) {
		super(new BorderLayout());
		this.controller = controller;
		this.thumbnailsPosition = thumbnailsPosition;
		declaredPerspective = perspective;
		this.slideshow = slideshow;
		for (int i = 0; i < slideshow.getPowerpointSlides().size(); i++) {
			PowerpointSlideView slideView = new PowerpointSlideView(slideshow.getPowerpointSlides().get(i), controller);
			slideViews.add(slideView);
			slidesPanel.add(slideView, String.valueOf(i));
		}
		thumbnailsPanel = new PowerpointSlideThumbnailsPanel(slideshow, this::slideChosen, thumbnailsPosition != ThumbnailsPosition.RIGHT);
		add(makeSplitPane(), BorderLayout.CENTER);
		browser = controller != null && !slideViews.isEmpty() ? new PowerpointSlideBrowser(slideViews.get(0).getSlide(), controller) : null;
		if (!slideViews.isEmpty()) {
			showSlide(slideViews.get(0).getSlide());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JXMultiSplitPane makeSplitPane() {
		MultiSplitLayoutFactory factory = new MultiSplitLayoutFactory.DefaultMultiSplitLayoutFactory();
		Split root = factory.makeSplit();
		Leaf<?> slideLeaf = factory.makeLeaf(SLIDE_LEAF);
		slideLeaf.setWeight(0.82);
		Leaf<?> thumbnailsLeaf = factory.makeLeaf(THUMBNAILS_LEAF);
		thumbnailsLeaf.setWeight(0.18);
		switch (thumbnailsPosition) {
			case TOP:
				root.setRowLayout(false);
				root.setChildren(thumbnailsLeaf, factory.makeDivider(), slideLeaf);
				break;
			case BOTTOM:
				root.setRowLayout(false);
				root.setChildren(slideLeaf, factory.makeDivider(), thumbnailsLeaf);
				break;
			default:
				root.setChildren(slideLeaf, factory.makeDivider(), thumbnailsLeaf);
		}
		MultiSplitLayout layout = new MultiSplitLayout(root, factory);
		layout.setLayoutByWeight(true);
		layout.setLayoutMode(MultiSplitLayout.NO_MIN_SIZE_LAYOUT);
		JXMultiSplitPane splitPane = new JXMultiSplitPane(layout);
		splitPane.setDividerPainter(new KnobDividerPainter());
		splitPane.add(slidesPanel, SLIDE_LEAF);
		splitPane.add(thumbnailsPanel, THUMBNAILS_LEAF);
		return splitPane;
	}

	/**
	 * Show the supplied slide in the center of the view, select its thumbnail, and make it the root of the browser
	 * 
	 * @param slide
	 */
	public void showSlide(PowerpointSlide slide) {
		int index = slideshow.getPowerpointSlides().indexOf(slide);
		if (index < 0) {
			return;
		}
		if (shownSlideView != slideViews.get(index)) {
			shownSlideView = slideViews.get(index);
			slidesLayout.show(slidesPanel, String.valueOf(index));
		}
		thumbnailsPanel.setSelectedSlide(slide);
		if (browser != null && browser.getRootObject() != slide) {
			browser.setRootObject(slide);
		}
	}

	/**
	 * Return the view of supplied slide, or null when it is not a slide of this slideshow
	 * 
	 * @param slide
	 * @return
	 */
	public PowerpointSlideView getSlideView(PowerpointSlide slide) {
		int index = slideshow.getPowerpointSlides().indexOf(slide);
		return index >= 0 && index < slideViews.size() ? slideViews.get(index) : null;
	}

	public ThumbnailsPosition getThumbnailsPosition() {
		return thumbnailsPosition;
	}

	/**
	 * The browser of the contents of the shown slide, which follows the shown slide, or null when the view has no controller
	 */
	public PowerpointSlideBrowser getSlideBrowser() {
		return browser;
	}

	/**
	 * Whether the view puts the browser of the shown slide in the bottom-left view of its perspective when it is shown, and removes it
	 * when it is hidden (the default)
	 */
	public boolean getInstallsBrowser() {
		return installsBrowser;
	}

	/**
	 * A module embedding the view, which places the browser of the shown slide ({@link #getSlideBrowser()}) itself, turns this off
	 * 
	 * @param installsBrowser
	 */
	public void setInstallsBrowser(boolean installsBrowser) {
		this.installsBrowser = installsBrowser;
	}

	public PowerpointElementDecorationProvider getDecorationProvider() {
		return decorationProvider;
	}

	/**
	 * Install supplied decoration provider on every slide of this view (or remove the current one, when null)
	 * 
	 * @param provider
	 */
	public void setDecorationProvider(PowerpointElementDecorationProvider provider) {
		decorationProvider = provider;
		for (PowerpointSlideView slideView : slideViews) {
			slideView.getEditor().getDrawing().setDecorationProvider(provider);
		}
	}

	/**
	 * Install on every slide of this view the elements that stand for an object selected elsewhere: see
	 * {@link PowerpointSlideEditor#setRepresentedElements(Function)}
	 * 
	 * @param representedElements
	 */
	public void setRepresentedElements(Function<FlexoObject, List<? extends PowerpointSlideElement>> representedElements) {
		for (PowerpointSlideView slideView : slideViews) {
			slideView.getEditor().setRepresentedElements(representedElements);
		}
	}

	public PowerpointSlide getShownSlide() {
		return shownSlideView != null ? shownSlideView.getSlide() : null;
	}

	/**
	 * Called when the user chooses a slide in the thumbnails: show it and select it
	 * 
	 * @param slide
	 */
	private void slideChosen(PowerpointSlide slide) {
		showSlide(slide);
		if (getSelectionManager() != null) {
			getSelectionManager().setSelectedObject(slide);
		}
	}

	public FlexoController getFlexoController() {
		return controller;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return declaredPerspective;
	}

	@Override
	public void deleteModuleView() {
		setDecorationProvider(null);
		for (PowerpointSlideView slideView : slideViews) {
			slideView.delete();
		}
		slideViews.clear();
		if (browser != null) {
			browser.deleteView();
		}
		if (getFlexoController() != null) {
			controller.removeModuleView(this);
		}
	}

	@Override
	public List<SelectionListener> getSelectionListeners() {
		Vector<SelectionListener> reply = new Vector<>();
		reply.add(this);
		return reply;
	}

	@Override
	public void willHide() {
		if (installsBrowser && browser != null && getPerspective() != null) {
			getPerspective().setBottomLeftView(null);
		}
	}

	@Override
	public void willShow() {
		// The contents of the slideshow are shown in the left side of the perspective
		if (installsBrowser && browser != null && getPerspective() != null) {
			getPerspective().setBottomLeftView(browser);
		}
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
	}

	@Override
	public PowerpointSlideshow getRepresentedObject() {
		return slideshow;
	}

	@Override
	public boolean isAutoscrolled() {
		return false;
	}

	@Override
	public void fireObjectSelected(FlexoObject object) {
		// Selection inside a slide is handled by the editor of the slide: only bring the slide to front
		PowerpointSlide slide = getSlide(object);
		if (slide != null) {
			showSlide(slide);
		}
	}

	/**
	 * Return the slide of this slideshow where the supplied object is located, or null
	 * 
	 * @param object
	 * @return
	 */
	private PowerpointSlide getSlide(FlexoObject object) {
		PowerpointSlide slide = null;
		if (object instanceof PowerpointSlide) {
			slide = (PowerpointSlide) object;
		}
		else if (object instanceof PowerpointSlideElement) {
			slide = ((PowerpointSlideElement) object).getPowerpointSlide();
		}
		if (slide != null && slide.getPowerpointSlideshow() == slideshow) {
			return slide;
		}
		return null;
	}

	@Override
	public void fireObjectDeselected(FlexoObject object) {
	}

	@Override
	public void fireResetSelection() {
	}

	@Override
	public void fireBeginMultipleSelection() {
	}

	@Override
	public void fireEndMultipleSelection() {
	}

	@Override
	public SelectionManager getSelectionManager() {
		if (getFlexoController() != null) {
			return getFlexoController().getSelectionManager();
		}
		return null;
	}

	@Override
	public Vector<FlexoObject> getSelection() {
		return getSelectionManager().getSelection();
	}

	@Override
	public void resetSelection() {
		getSelectionManager().resetSelection();
	}

	@Override
	public void addToSelected(FlexoObject object) {
		getSelectionManager().addToSelected(object);
	}

	@Override
	public void removeFromSelected(FlexoObject object) {
		getSelectionManager().removeFromSelected(object);
	}

	@Override
	public void addToSelected(Vector<? extends FlexoObject> objects) {
		getSelectionManager().addToSelected(objects);
	}

	@Override
	public void removeFromSelected(Vector<? extends FlexoObject> objects) {
		getSelectionManager().removeFromSelected(objects);
	}
	
	@Override
	public void setSelectedObjects(Vector<? extends FlexoObject> objects) {
		getSelectionManager().setSelectedObjects(objects);
	}

	@Override
	public FlexoObject getFocusedObject() {
		return getSelectionManager().getFocusedObject();
	}

	@Override
	public boolean mayRepresents(FlexoObject anObject) {
		return anObject == slideshow || getSlide(anObject) != null;
	}

}
