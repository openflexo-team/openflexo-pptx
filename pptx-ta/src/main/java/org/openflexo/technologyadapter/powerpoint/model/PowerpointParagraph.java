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

package org.openflexo.technologyadapter.powerpoint.model;

import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Represents a paragraph of a {@link PowerpointTextShape}, wrapping the POI {@link XSLFTextParagraph} concept: one item of a bulleted
 * list, for instance
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointParagraph.PowerpointParagraphImpl.class)
public interface PowerpointParagraph extends PowerpointSlideElement {

	@PropertyIdentifier(type = PowerpointTextShape.class)
	public static final String TEXT_SHAPE_KEY = "textShape";
	@PropertyIdentifier(type = XSLFTextParagraph.class)
	public static final String PARAGRAPH_KEY = "paragraph";

	@Getter(value = TEXT_SHAPE_KEY, inverse = PowerpointTextShape.PARAGRAPHS_KEY)
	public PowerpointTextShape getTextShape();

	@Setter(TEXT_SHAPE_KEY)
	public void setTextShape(PowerpointTextShape textShape);

	/**
	 * Return the POI {@link XSLFTextParagraph} wrapped by this {@link PowerpointParagraph}
	 *
	 * @return
	 */
	@Getter(value = PARAGRAPH_KEY, ignoreType = true)
	public XSLFTextParagraph getParagraph();

	@Setter(PARAGRAPH_KEY)
	public void setParagraph(XSLFTextParagraph paragraph);

	/**
	 * Return the text of this paragraph
	 *
	 * @return
	 */
	public String getText();

	/**
	 * Return the indentation level of this paragraph (0 for the first level of a list)
	 *
	 * @return
	 */
	public int getLevel();

	/**
	 * Return whether this paragraph is an item of a list (with a bullet or a number)
	 *
	 * @return
	 */
	public boolean isBullet();

	/**
	 * Return the bullet character of this paragraph, or null
	 *
	 * @return
	 */
	public String getBulletCharacter();

	/**
	 * Return the 0-based index of this paragraph in its text
	 *
	 * @return
	 */
	public int getIndex();

	/**
	 * Default base implementation for {@link PowerpointParagraph}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointParagraphImpl extends PowerpointObjectImpl implements PowerpointParagraph {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointParagraphImpl.class.getPackage().getName());

		@Override
		public PowerpointSlide getPowerpointSlide() {
			return getTextShape() != null ? getTextShape().getPowerpointSlide() : null;
		}

		@Override
		public PowerpointSlideshow getResourceData() {
			return getTextShape() != null ? getTextShape().getResourceData() : null;
		}

		@Override
		public String getText() {
			return getParagraph() != null ? getParagraph().getText() : null;
		}

		@Override
		public int getLevel() {
			return getParagraph() != null ? getParagraph().getIndentLevel() : 0;
		}

		@Override
		public boolean isBullet() {
			return getParagraph() != null && getParagraph().isBullet();
		}

		@Override
		public String getBulletCharacter() {
			return isBullet() ? getParagraph().getBulletCharacter() : null;
		}

		@Override
		public int getIndex() {
			return getTextShape() != null ? getTextShape().getParagraphs().indexOf(this) : -1;
		}

		/**
		 * The name of a paragraph is its text, so that a text reads well in a browser
		 */
		@Override
		public String getName() {
			String text = getText();
			if (text == null || text.trim().isEmpty()) {
				return "(empty paragraph)";
			}
			text = text.trim().replace('\n', ' ');
			return text.length() > 60 ? text.substring(0, 60) + "…" : text;
		}

		@Override
		public String getUri() {
			return (getTextShape() != null ? getTextShape().getUri() : "pptx:text") + "/paragraph[" + getIndex() + "]";
		}
	}
}
