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

import org.apache.poi.xslf.usermodel.XSLFTable;
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
 * Represents a table of a slide, wrapping the POI {@link XSLFTable} concept
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(value = PowerpointTable.PowerpointTableImpl.class)
public interface PowerpointTable extends PowerpointShape {

	@PropertyIdentifier(type = PowerpointTableRow.class, cardinality = Cardinality.LIST)
	public static final String ROWS_KEY = "rows";

	/**
	 * Return the rows of this table, from top to bottom
	 *
	 * @return
	 */
	@Getter(value = ROWS_KEY, cardinality = Cardinality.LIST, inverse = PowerpointTableRow.TABLE_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<PowerpointTableRow> getRows();

	@Setter(ROWS_KEY)
	public void setRows(List<PowerpointTableRow> rows);

	@Adder(ROWS_KEY)
	@PastingPoint
	public void addToRows(PowerpointTableRow aRow);

	@Remover(ROWS_KEY)
	public void removeFromRows(PowerpointTableRow aRow);

	/**
	 * Return the number of columns of this table
	 *
	 * @return
	 */
	public int getNumberOfColumns();

	/**
	 * Return the cell at the supplied row and column (0-based), or null
	 *
	 * @param row
	 * @param column
	 * @return
	 */
	public PowerpointTableCell getCell(int row, int column);

	/**
	 * Default base implementation for {@link PowerpointTable}
	 *
	 * @author sylvain
	 *
	 */
	public static abstract class PowerpointTableImpl extends PowerpointShapeImpl implements PowerpointTable {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(PowerpointTableImpl.class.getPackage().getName());

		@Override
		public int getNumberOfColumns() {
			return getShape() instanceof XSLFTable ? ((XSLFTable) getShape()).getNumberOfColumns() : 0;
		}

		@Override
		public PowerpointTableCell getCell(int row, int column) {
			if (row < 0 || row >= getRows().size()) {
				return null;
			}
			List<PowerpointTableCell> cells = getRows().get(row).getCells();
			return column >= 0 && column < cells.size() ? cells.get(column) : null;
		}
	}
}
