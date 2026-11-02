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

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * Helpers reading DrawingML properties that POI does not expose (colors of the charts, for instance)
 *
 * @author sylvain
 *
 */
class PowerpointXmlUtils {

	static final String CHART_NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/chart";
	static final String DRAWING_NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/main";

	private PowerpointXmlUtils() {
	}

	/**
	 * Return the value of an attribute of the first descendant of the supplied element with the supplied name, or null
	 */
	static String findAttribute(XmlObject scope, String elementName, String attributeName) {
		if (scope == null) {
			return null;
		}
		try (XmlCursor cursor = scope.newCursor(); XmlCursor end = cursor.newCursor()) {
			end.toEndToken();
			while (cursor.toNextToken() != XmlCursor.TokenType.NONE && cursor.isLeftOf(end)) {
				if (cursor.isStart() && elementName.equals(cursor.getName().getLocalPart())) {
					return cursor.getAttributeText(new QName(attributeName));
				}
			}
		}
		return null;
	}

	/**
	 * Return the first child of the supplied element with the supplied namespace and name, or null
	 */
	static XmlObject firstChild(XmlObject parent, String namespace, String localName) {
		if (parent == null) {
			return null;
		}
		XmlObject[] children = parent.selectChildren(namespace, localName);
		return children.length > 0 ? children[0] : null;
	}

	/**
	 * Return the RGB color of the solid fill directly held by the supplied shape properties (spPr), or null
	 */
	static Color solidFillColor(XmlObject shapeProperties) {
		return toColor(findAttribute(firstChild(shapeProperties, DRAWING_NAMESPACE, "solidFill"), "srgbClr", "val"));
	}

	/**
	 * Return the RGB color of the line of the supplied shape properties (spPr), or null
	 */
	static Color lineColor(XmlObject shapeProperties) {
		return toColor(findAttribute(firstChild(firstChild(shapeProperties, DRAWING_NAMESPACE, "ln"), DRAWING_NAMESPACE, "solidFill"),
				"srgbClr", "val"));
	}

	static Color toColor(String rgb) {
		if (rgb == null || rgb.length() != 6) {
			return null;
		}
		try {
			return new Color(Integer.parseInt(rgb, 16));
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
