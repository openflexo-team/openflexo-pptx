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

import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Represents a row of a {@link PowerpointTable}, wrapping the POI {@link XSLFTableRow} concept
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointTableRow.PowerpointTableRowImpl.class)
public interface PowerpointTableRow extends PowerpointSlideElement {

	@PropertyIdentifier(type = PowerpointTable.class)
	public static final String TABLE_KEY = "table";
	@PropertyIdentifier(type = XSLFTableRow.class)
	public static final String ROW_KEY = "row";
	@PropertyIdentifier(type = PowerpointTableCell.class, cardinality = Cardinality.LIST)
	public static final String CELLS_KEY = "cells";

	@Getter(value = TABLE_KEY, inverse = PowerpointTable.ROWS_KEY)
	public PowerpointTable getTable();

	@Setter(TABLE_KEY)
	public void setTable(PowerpointTable table);

	/**
	 * Return the POI {@link XSLFTableRow} wrapped by this {@link PowerpointTableRow}
	 *
	 * @return
	 */
	@Getter(value = ROW_KEY, ignoreType = true)
	public XSLFTableRow getRow();

	@Setter(ROW_KEY)
	public void setRow(XSLFTableRow row);

	/**
	 * Return the cells of this row, from left to right
	 *
	 * @return
	 */
	@Getter(value = CELLS_KEY, cardinality = Cardinality.LIST, inverse = PowerpointTableCell.TABLE_ROW_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<PowerpointTableCell> getCells();

	@Setter(CELLS_KEY)
	public void setCells(List<PowerpointTableCell> cells);

	@Adder(CELLS_KEY)
	@PastingPoint
	public void addToCells(PowerpointTableCell aCell);

	@Remover(CELLS_KEY)
	public void removeFromCells(PowerpointTableCell aCell);

	/**
	 * Return the 0-based index of this row in its table
	 *
	 * @return
	 */
	public int getRowIndex();

	/**
	 * Return the height of this row, in points
	 *
	 * @return
	 */
	public double getHeight();

	/**
	 * Default base implementation for {@link PowerpointTableRow}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointTableRowImpl extends PowerpointObjectImpl implements PowerpointTableRow {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointTableRowImpl.class.getPackage().getName());

		@Override
		public PowerpointSlide getPowerpointSlide() {
			return getTable() != null ? getTable().getPowerpointSlide() : null;
		}

		@Override
		public PowerpointSlideshow getResourceData() {
			return getTable() != null ? getTable().getResourceData() : null;
		}

		@Override
		public int getRowIndex() {
			return getTable() != null ? getTable().getRows().indexOf(this) : -1;
		}

		@Override
		public double getHeight() {
			return getRow() != null ? getRow().getHeight() : 0;
		}

		@Override
		public String getName() {
			return "Row " + (getRowIndex() + 1);
		}

		@Override
		public String getUri() {
			return (getTable() != null ? getTable().getUri() : "pptx:table") + "/row[" + getRowIndex() + "]";
		}
	}
}
