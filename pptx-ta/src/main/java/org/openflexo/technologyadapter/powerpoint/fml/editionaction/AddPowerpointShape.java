/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.technologyadapter.powerpoint.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.technologyadapter.powerpoint.BasicPowerpointModelSlot;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointShape;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointTextBox;

/**
 * FML edition action creating a new text box (backed by a POI XSLFTextBox) in a target {@link PowerpointSlide}.
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(AddPowerpointShape.AddPowerpointShapeImpl.class)
@XMLElement
@FML("AddPowerpointShape")
public interface AddPowerpointShape extends PowerpointAction<PowerpointShape> {

	public DataBinding<PowerpointSlide> getPowerpointSlide();

	public void setPowerpointSlide(DataBinding<PowerpointSlide> powerpointSlide);

	public DataBinding<String> getText();

	public void setText(DataBinding<String> text);

	public static abstract class AddPowerpointShapeImpl
			extends TechnologySpecificActionDefiningReceiverImpl<BasicPowerpointModelSlot, PowerpointSlideshow, PowerpointShape>
			implements AddPowerpointShape {

		private static final Logger logger = Logger.getLogger(AddPowerpointShape.class.getPackage().getName());

		private DataBinding<PowerpointSlide> powerpointSlide;
		private DataBinding<String> text;

		@Override
		public Type getAssignableType() {
			return PowerpointShape.class;
		}

		@Override
		public PowerpointShape execute(RunTimeEvaluationContext evaluationContext) {

			PowerpointSlideshow receiver = getReceiver(evaluationContext);

			try {
				PowerpointSlide powerpointSlide = getPowerpointSlide().getBindingValue(evaluationContext);
				if (powerpointSlide != null) {
					String textValue = getText().isSet() ? getText().getBindingValue(evaluationContext) : "";
					PowerpointTextBox textBox = powerpointSlide.addTextBox(textValue != null ? textValue : "", 50, 50, 300, 50);
					if (receiver != null) {
						receiver.setIsModified();
					}
					return textBox;
				}
				logger.warning("Creating a shape requires a target slide");
			} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException e) {
				logger.log(Level.WARNING, "Cannot evaluate binding while adding a PowerPoint shape", e);
			}

			return null;
		}

		@Override
		public DataBinding<PowerpointSlide> getPowerpointSlide() {
			if (powerpointSlide == null) {
				powerpointSlide = new DataBinding<>(this, PowerpointSlide.class, DataBinding.BindingDefinitionType.GET);
				powerpointSlide.setBindingName("powerpointSlide");
			}
			return powerpointSlide;
		}

		@Override
		public void setPowerpointSlide(DataBinding<PowerpointSlide> powerpointSlide) {
			if (powerpointSlide != null) {
				powerpointSlide.setOwner(this);
				powerpointSlide.setDeclaredType(PowerpointSlide.class);
				powerpointSlide.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				powerpointSlide.setBindingName("powerpointSlide");
			}
			this.powerpointSlide = powerpointSlide;
		}

		@Override
		public DataBinding<String> getText() {
			if (text == null) {
				text = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				text.setBindingName("text");
			}
			return text;
		}

		@Override
		public void setText(DataBinding<String> text) {
			if (text != null) {
				text.setOwner(this);
				text.setDeclaredType(String.class);
				text.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				text.setBindingName("text");
			}
			this.text = text;
		}

	}

}
