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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Represents a PowerPoint shape carrying text (backed by a POI XSLFTextShape).
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
public interface PowerpointTextShape extends PowerpointSimpleShape {

	@PropertyIdentifier(type = PowerpointParagraph.class, cardinality = Cardinality.LIST)
	public static final String PARAGRAPHS_KEY = "paragraphs";

	/**
	 * Return the paragraphs of the text of this shape
	 *
	 * @return
	 */
	@Getter(value = PARAGRAPHS_KEY, cardinality = Cardinality.LIST, inverse = PowerpointParagraph.TEXT_SHAPE_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<PowerpointParagraph> getParagraphs();

	@Setter(PARAGRAPHS_KEY)
	public void setParagraphs(List<PowerpointParagraph> paragraphs);

	@Adder(PARAGRAPHS_KEY)
	@PastingPoint
	public void addToParagraphs(PowerpointParagraph aParagraph);

	@Remover(PARAGRAPHS_KEY)
	public void removeFromParagraphs(PowerpointParagraph aParagraph);

	/**
	 * Build the paragraphs of this shape again from the text of the underlying POI shape
	 *
	 * @param factory
	 *            the factory used to create the paragraphs
	 */
	public void updateParagraphs(PowerpointModelFactory factory);

	/**
	 * Return the first paragraph of this shape, or null when it has none (a binding can use it where
	 * {@code paragraphs.get(0)} would throw on an empty list)
	 *
	 * @return
	 */
	public PowerpointParagraph getFirstParagraph();

	/**
	 * Return the text held by this shape
	 *
	 * @return
	 */
	public String getText();

	/**
	 * Sets the text held by this shape (and propagates it to the underlying POI shape)
	 *
	 * @param text
	 */
	public void setText(String text);

	/**
	 * Default base implementation for {@link PowerpointTextShape}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointTextShapeImpl extends PowerpointSimpleShapeImpl implements PowerpointTextShape {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointTextShapeImpl.class.getPackage().getName());

		@Override
		public void updateParagraphs(PowerpointModelFactory factory) {
			for (PowerpointParagraph paragraph : new ArrayList<>(getParagraphs())) {
				removeFromParagraphs(paragraph);
			}
			if (getShape() instanceof XSLFTextShape) {
				for (XSLFTextParagraph poiParagraph : ((XSLFTextShape) getShape()).getTextParagraphs()) {
					PowerpointParagraph paragraph = factory.makePowerpointParagraph();
					paragraph.setParagraph(poiParagraph);
					addToParagraphs(paragraph);
				}
			}
		}

		@Override
		public PowerpointParagraph getFirstParagraph() {
			return getParagraphs().isEmpty() ? null : getParagraphs().get(0);
		}

		@Override
		public String getText() {
			if (getShape() instanceof XSLFTextShape) {
				return ((XSLFTextShape) getShape()).getText();
			}
			return null;
		}

		@Override
		public void setText(String text) {
			if (getShape() instanceof XSLFTextShape) {
				((XSLFTextShape) getShape()).setText(text);
				if (getFactory() != null) {
					updateParagraphs(getFactory());
				}
			}
		}

	}

}
