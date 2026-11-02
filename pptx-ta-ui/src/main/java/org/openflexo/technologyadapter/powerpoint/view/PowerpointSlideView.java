/**
 * 
 * Copyright (c) 2014, Openflexo
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
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.view.controller.FlexoController;

/**
 * Widget showing a {@link PowerpointSlide}, whose shapes can be selected.<br>
 * The slide is rendered by POI, inside a {@link PowerpointSlideEditor} that provides focus and selection. It is centered and zoomed to
 * fit the available space, so that no scroll bar is needed.
 * 
 * @author vincent, sguerin
 * 
 */
@SuppressWarnings("serial")
public class PowerpointSlideView extends JPanel {
	static final Logger logger = Logger.getLogger(PowerpointSlideView.class.getPackage().getName());

	/**
	 * Margin around the slide, in pixels
	 */
	private static final int MARGIN = 10;

	private final PowerpointSlide slide;
	private final PowerpointSlideEditor editor;

	public PowerpointSlideView(PowerpointSlide slide, FlexoController controller) {
		super(new BorderLayout());
		this.slide = slide;
		editor = new PowerpointSlideEditor(slide, controller);
		// A GridBagLayout with a single component keeps it centered at its preferred size
		JPanel centeringPanel = new JPanel(new GridBagLayout());
		centeringPanel.add(editor.getDrawingView());
		add(centeringPanel, BorderLayout.CENTER);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				zoomToFit();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				zoomToFit();
			}
		});
	}

	/**
	 * Set the scale of the editor so that the whole slide fits in the space available in this view
	 */
	public void zoomToFit() {
		double pageWidth = slide.getPowerpointSlideshow().getPageWidth();
		double pageHeight = slide.getPowerpointSlideshow().getPageHeight();
		int availableWidth = getWidth() - 2 * MARGIN;
		int availableHeight = getHeight() - 2 * MARGIN;
		if (!isVisible() || pageWidth <= 0 || pageHeight <= 0 || availableWidth <= 0 || availableHeight <= 0) {
			return;
		}
		double scale = Math.min(availableWidth / pageWidth, availableHeight / pageHeight);
		if (Math.abs(scale - editor.getScale()) > 0.001) {
			editor.setScale(scale);
			revalidate();
		}
	}

	public PowerpointSlide getSlide() {
		return slide;
	}

	public PowerpointSlideEditor getEditor() {
		return editor;
	}

	public void delete() {
		editor.delete();
	}

}
