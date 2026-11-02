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

import java.awt.Color;
import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Represents a cell of a {@link PowerpointTable}, wrapping the POI {@link XSLFTableCell} concept
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointTableCell.PowerpointTableCellImpl.class)
public interface PowerpointTableCell extends PowerpointSlideElement {

	@PropertyIdentifier(type = PowerpointTableRow.class)
	public static final String TABLE_ROW_KEY = "tableRow";
	@PropertyIdentifier(type = XSLFTableCell.class)
	public static final String CELL_KEY = "cell";

	@Getter(value = TABLE_ROW_KEY, inverse = PowerpointTableRow.CELLS_KEY)
	public PowerpointTableRow getTableRow();

	@Setter(TABLE_ROW_KEY)
	public void setTableRow(PowerpointTableRow row);

	/**
	 * Return the POI {@link XSLFTableCell} wrapped by this {@link PowerpointTableCell}
	 *
	 * @return
	 */
	@Getter(value = CELL_KEY, ignoreType = true)
	public XSLFTableCell getCell();

	@Setter(CELL_KEY)
	public void setCell(XSLFTableCell cell);

	/**
	 * Return the text of this cell
	 *
	 * @return
	 */
	public String getText();

	/**
	 * Return the 0-based index of the row of this cell
	 *
	 * @return
	 */
	public int getRowIndex();

	/**
	 * Return the 0-based index of the column of this cell
	 *
	 * @return
	 */
	public int getColumnIndex();

	/**
	 * Return the number of columns this cell spans
	 *
	 * @return
	 */
	public int getGridSpan();

	/**
	 * Return the number of rows this cell spans
	 *
	 * @return
	 */
	public int getRowSpan();

	/**
	 * Return the solid fill color of this cell, or null when it has none
	 *
	 * @return
	 */
	public Color getFillColor();

	/**
	 * Default base implementation for {@link PowerpointTableCell}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointTableCellImpl extends PowerpointObjectImpl implements PowerpointTableCell {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointTableCellImpl.class.getPackage().getName());

		@Override
		public PowerpointSlide getPowerpointSlide() {
			return getTableRow() != null ? getTableRow().getPowerpointSlide() : null;
		}

		@Override
		public PowerpointSlideshow getResourceData() {
			return getTableRow() != null ? getTableRow().getResourceData() : null;
		}

		@Override
		public String getText() {
			return getCell() != null ? getCell().getText() : null;
		}

		@Override
		public int getRowIndex() {
			return getTableRow() != null ? getTableRow().getRowIndex() : -1;
		}

		@Override
		public int getColumnIndex() {
			return getTableRow() != null ? getTableRow().getCells().indexOf(this) : -1;
		}

		@Override
		public int getGridSpan() {
			return getCell() != null ? getCell().getGridSpan() : 1;
		}

		@Override
		public int getRowSpan() {
			return getCell() != null ? getCell().getRowSpan() : 1;
		}

		@Override
		public Color getFillColor() {
			return getCell() != null ? getCell().getFillColor() : null;
		}

		/**
		 * The name of a cell is its text, so that a table reads well in a browser
		 */
		@Override
		public String getName() {
			String text = getText();
			if (text == null || text.trim().isEmpty()) {
				return "Cell [" + (getRowIndex() + 1) + "," + (getColumnIndex() + 1) + "]";
			}
			text = text.trim().replace('\n', ' ');
			return text.length() > 40 ? text.substring(0, 40) + "…" : text;
		}

		@Override
		public String getUri() {
			return (getTableRow() != null ? getTableRow().getUri() : "pptx:row") + "/cell[" + getColumnIndex() + "]";
		}
	}
}
