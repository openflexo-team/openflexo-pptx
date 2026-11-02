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
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;

/**
 * Vertical list of the thumbnails of the slides of a {@link PowerpointSlideshow}, used to choose the slide to show.<br>
 * Thumbnails are rendered by POI the first time they are displayed, then cached. Their width follows the width of the panel: room is
 * always kept for the vertical scroll bar, so that its appearance does not change the width again, and there is never a horizontal one.
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class PowerpointSlideThumbnailsPanel extends JPanel {

	private static final Logger logger = Logger.getLogger(PowerpointSlideThumbnailsPanel.class.getPackage().getName());

	/**
	 * Minimal width of a thumbnail, in pixels
	 */
	private static final int MIN_THUMBNAIL_WIDTH = 40;

	/**
	 * Space taken around a thumbnail by the borders of its cell, in pixels (both sides)
	 */
	private static final int CELL_BORDERS = 14;

	private static final int ICON_TEXT_GAP = 6;

	/**
	 * Delay before thumbnails are rendered again at a new width, in milliseconds
	 */
	private static final int RESIZE_DELAY = 150;

	private static final Color SELECTION_COLOR = new Color(0xD2, 0x47, 0x26);

	private final PowerpointSlideshow slideshow;
	private final JList<PowerpointSlide> list;
	private final Map<PowerpointSlide, ImageIcon> thumbnails = new HashMap<>();
	private boolean updatingSelection = false;
	private final JScrollPane scrollPane;
	private final Timer resizeTimer;
	private int thumbnailWidth = 160;

	/**
	 * @param slideshow
	 *            the slideshow whose slides are listed
	 * @param slideChooser
	 *            called with the slide chosen by the user
	 */
	public PowerpointSlideThumbnailsPanel(PowerpointSlideshow slideshow, Consumer<PowerpointSlide> slideChooser) {
		super(new BorderLayout());
		this.slideshow = slideshow;
		list = new JList<>(slideshow.getPowerpointSlides().toArray(new PowerpointSlide[0]));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new ThumbnailRenderer());
		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && !updatingSelection && list.getSelectedValue() != null) {
				slideChooser.accept(list.getSelectedValue());
			}
		});
		scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
		resizeTimer = new Timer(RESIZE_DELAY, e -> adjustThumbnailWidth());
		resizeTimer.setRepeats(false);
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				resizeTimer.restart();
			}
		});
	}

	/**
	 * Compute the width of the thumbnails from the width of the panel, and render them again when it changed
	 */
	public void adjustThumbnailWidth() {
		int numberWidth = list.getFontMetrics(list.getFont()).stringWidth(String.valueOf(slideshow.getPowerpointSlides().size()));
		int available = scrollPane.getWidth() - scrollPane.getInsets().left - scrollPane.getInsets().right
				- scrollPane.getVerticalScrollBar().getPreferredSize().width - CELL_BORDERS - numberWidth - ICON_TEXT_GAP;
		int newWidth = Math.max(MIN_THUMBNAIL_WIDTH, available);
		if (newWidth != thumbnailWidth) {
			thumbnailWidth = newWidth;
			thumbnails.clear();
			// Setting the renderer again makes the list compute the size of its cells again
			list.setCellRenderer(new ThumbnailRenderer());
			list.revalidate();
			list.repaint();
			if (list.getSelectedIndex() >= 0) {
				list.ensureIndexIsVisible(list.getSelectedIndex());
			}
		}
	}

	public int getThumbnailWidth() {
		return thumbnailWidth;
	}

	/**
	 * Select the thumbnail of the supplied slide, without notifying the slide chooser
	 * 
	 * @param slide
	 */
	public void setSelectedSlide(PowerpointSlide slide) {
		if (list.getSelectedValue() == slide) {
			return;
		}
		updatingSelection = true;
		try {
			list.setSelectedValue(slide, true);
		} finally {
			updatingSelection = false;
		}
	}

	public PowerpointSlide getSelectedSlide() {
		return list.getSelectedValue();
	}

	private ImageIcon getThumbnail(PowerpointSlide slide) {
		return thumbnails.computeIfAbsent(slide, this::renderThumbnail);
	}

	private ImageIcon renderThumbnail(PowerpointSlide slide) {
		double pageWidth = slideshow.getPageWidth();
		double pageHeight = slideshow.getPageHeight();
		double scale = pageWidth > 0 ? thumbnailWidth / pageWidth : 1;
		BufferedImage image = new BufferedImage(thumbnailWidth, Math.max(1, (int) Math.round(pageHeight * scale)),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.scale(scale, scale);
			PowerpointDrawFactory.install(g, slide);
			slide.getSlide().draw(g);
		} catch (RuntimeException e) {
			// POI may fail on some contents: keep a blank thumbnail
			logger.log(Level.WARNING, "Could not render thumbnail of slide " + slide.getName(), e);
		} finally {
			g.dispose();
		}
		return new ImageIcon(image);
	}

	private class ThumbnailRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, false, false);
			PowerpointSlide slide = (PowerpointSlide) value;
			setText(String.valueOf(slide.getSlideNumber() + 1));
			setIcon(getThumbnail(slide));
			setHorizontalTextPosition(SwingConstants.LEADING);
			setVerticalTextPosition(SwingConstants.TOP);
			setIconTextGap(ICON_TEXT_GAP);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
					isSelected ? BorderFactory.createLineBorder(SELECTION_COLOR, 3)
							: BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
									BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1))));
			return this;
		}
	}
}
