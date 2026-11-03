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

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideElement;

/**
 * Overlay hook of the slideshow view, so that a client module (the FreeModellingEditor for instance) shows its own information over the
 * elements of the slides without modifying the presentation.<br>
 * The provider maps an element of a slide to a {@link Decoration} - an optional border color, fill tint and badge text - painted on the
 * transparent Diana shape that covers the element. Install it with
 * {@link PowerpointSlideshowView#setDecorationProvider(PowerpointElementDecorationProvider)}, and call {@link #refresh()} whenever the
 * decorations change.
 * 
 * @author sylvain
 * 
 */
public abstract class PowerpointElementDecorationProvider {

	/** Property fired by {@link #refresh()} */
	public static final String DECORATIONS = "decorations";

	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

	/**
	 * Return the decoration of supplied element, or null to leave it undecorated
	 * 
	 * @param element
	 * @return
	 */
	public abstract Decoration getDecoration(PowerpointSlideElement element);

	/**
	 * Notify the views where this provider is installed that the decorations changed: they ask again the decoration of every element
	 */
	public void refresh() {
		pcSupport.firePropertyChange(DECORATIONS, null, this);
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcSupport.removePropertyChangeListener(listener);
	}

	/**
	 * What is painted over an element: each part is optional (null)
	 */
	public static class Decoration {

		private final Color borderColor;
		private final Color fillTint;
		private final String badge;

		/**
		 * @param borderColor
		 *            color of a border drawn around the element
		 * @param fillTint
		 *            color painted over the element, with the opacity given by its alpha (a color with no alpha would hide the element)
		 * @param badge
		 *            short text shown at the top right corner of the element
		 */
		public Decoration(Color borderColor, Color fillTint, String badge) {
			this.borderColor = borderColor;
			this.fillTint = fillTint;
			this.badge = badge;
		}

		public Color getBorderColor() {
			return borderColor;
		}

		public Color getFillTint() {
			return fillTint;
		}

		public String getBadge() {
			return badge;
		}
	}

}
