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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.openflexo.diana.DianaCoreUtils;
import org.openflexo.diana.Drawing.ContainerNode;
import org.openflexo.diana.Drawing.DrawingTreeNode;
import org.openflexo.diana.swing.control.SwingToolFactory;
import org.openflexo.selection.SelectionManagingDianaEditor;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlide;
import org.openflexo.view.controller.FlexoController;

/**
 * Read-only Diana editor of a {@link PowerpointSlide}: shapes can be focused and selected, the selection being synchronized with the
 * {@link org.openflexo.selection.SelectionManager} of the {@link FlexoController}.<br>
 * A click selects the deepest element under the mouse; ESCAPE then selects the element containing the selected one (a group, a table, a
 * row; the slide for a top-level shape), and ENTER selects the first element contained in the selected one.
 * 
 * @author sylvain
 * 
 */
public class PowerpointSlideEditor extends SelectionManagingDianaEditor<PowerpointSlide> {

	public PowerpointSlideEditor(PowerpointSlide slide, FlexoController controller) {
		super(new PowerpointSlideDrawing(slide), controller != null ? controller.getSelectionManager() : null, DianaCoreUtils.TOOLS_FACTORY,
				SwingToolFactory.DEFAULT);
		InputMap inputMap = getDrawingView().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "select_container");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select_first_child");
		getDrawingView().getActionMap().put("select_container", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectContainer();
			}
		});
		getDrawingView().getActionMap().put("select_first_child", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectFirstChild();
			}
		});
	}

	/**
	 * Return the node selected alone, or null when the selection is empty or multiple
	 * 
	 * @return
	 */
	private DrawingTreeNode<?, ?> getSingleSelectedNode() {
		List<DrawingTreeNode<?, ?>> selection = getSelectedObjects();
		return selection.size() == 1 ? selection.get(0) : null;
	}

	/**
	 * Select the element containing the selected one (a group, a table, a row), or the slide for a top-level shape
	 */
	public void selectContainer() {
		DrawingTreeNode<?, ?> node = getSingleSelectedNode();
		if (node == null || node.getParentNode() == null) {
			return;
		}
		if (node.getParentNode() != getDrawing().getRoot()) {
			setSelectedObject(node.getParentNode());
		}
		else {
			setSelectedObjects(Collections.emptyList());
			selectDrawing();
		}
	}

	/**
	 * Select the first element contained in the selected one (the first shape of a group, the first row of a table, the first cell of a
	 * row)
	 */
	public void selectFirstChild() {
		DrawingTreeNode<?, ?> node = getSingleSelectedNode();
		if (node instanceof ContainerNode && node != getDrawing().getRoot() && !((ContainerNode<?, ?>) node).getChildNodes().isEmpty()) {
			setSelectedObject(((ContainerNode<?, ?>) node).getChildNodes().get(0));
		}
	}

	@Override
	public PowerpointSlideDrawing getDrawing() {
		return (PowerpointSlideDrawing) super.getDrawing();
	}

	public PowerpointSlide getSlide() {
		return getDrawing().getModel();
	}
}
