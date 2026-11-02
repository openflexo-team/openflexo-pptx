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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * Builds the paint of a DrawingML pattern fill ({@code a:pattFill}), which POI does not render: a 8x8 tile of the preset pattern, in its
 * foreground and background colors.<br>
 * As in PowerPoint, a pattern pixel is a pixel of the device, whatever the zoom.
 * 
 * @author sylvain
 * 
 */
public class PowerpointPatternFill {

	private static final int SIZE = 8;

	/**
	 * Ordered dithering matrix, used for the percentage patterns
	 */
	private static final int[][] BAYER = { { 0, 32, 8, 40, 2, 34, 10, 42 }, { 48, 16, 56, 24, 50, 18, 58, 26 },
			{ 12, 44, 4, 36, 14, 46, 6, 38 }, { 60, 28, 52, 20, 62, 30, 54, 22 }, { 3, 35, 11, 43, 1, 33, 9, 41 },
			{ 51, 19, 59, 27, 49, 17, 57, 25 }, { 15, 47, 7, 39, 13, 45, 5, 37 }, { 63, 31, 55, 23, 61, 29, 53, 21 } };

	private PowerpointPatternFill() {
	}

	/**
	 * Return the pattern fill held by the child of the supplied element with the supplied name (spPr for a shape, bgPr for a
	 * background), or null
	 * 
	 * @param element
	 * @param propertiesName
	 * @return
	 */
	public static XmlObject findPatternFill(XmlObject element, String propertiesName) {
		XmlObject properties = child(element, propertiesName);
		return properties != null ? child(properties, "pattFill") : null;
	}

	private static XmlObject child(XmlObject parent, String localName) {
		if (parent == null) {
			return null;
		}
		try (XmlCursor cursor = parent.newCursor()) {
			if (cursor.toFirstChild()) {
				do {
					if (localName.equals(cursor.getName().getLocalPart())) {
						return cursor.getObject();
					}
				} while (cursor.toNextSibling());
			}
		}
		return null;
	}

	/**
	 * Return the paint of the supplied pattern fill, for the device of the supplied graphics
	 * 
	 * @param patternFill
	 *            a {@code a:pattFill} element
	 * @param graphics
	 * @return
	 */
	public static Paint makePaint(XmlObject patternFill, Graphics2D graphics) {
		String preset = attribute(patternFill, "prst");
		Color foreground = color(child(patternFill, "fgClr"), Color.BLACK);
		Color background = color(child(patternFill, "bgClr"), Color.WHITE);
		BufferedImage tile = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				tile.setRGB(x, y, (isForeground(preset != null ? preset : "pct50", x, y) ? foreground : background).getRGB());
			}
		}
		// A pattern pixel is a device pixel: one tile is SIZE pixels of the device, whatever the transform of the graphics
		double scale = Math.sqrt(Math.abs(graphics.getTransform().getDeterminant()));
		double cell = scale > 0 ? 1 / scale : 1;
		return new TexturePaint(tile, new Rectangle2D.Double(0, 0, SIZE * cell, SIZE * cell));
	}

	private static String attribute(XmlObject element, String name) {
		if (element == null) {
			return null;
		}
		try (XmlCursor cursor = element.newCursor()) {
			return cursor.getAttributeText(new QName(name));
		}
	}

	/**
	 * Return the RGB color held by the supplied color element (srgbClr, or a preset color), or the default color
	 */
	private static Color color(XmlObject colorElement, Color defaultColor) {
		XmlObject rgb = child(colorElement, "srgbClr");
		if (rgb != null) {
			try {
				return new Color(Integer.parseInt(attribute(rgb, "val"), 16));
			} catch (NumberFormatException | NullPointerException e) {
				return defaultColor;
			}
		}
		XmlObject preset = child(colorElement, "prstClr");
		if (preset != null) {
			if ("white".equals(attribute(preset, "val"))) {
				return Color.WHITE;
			}
			if ("black".equals(attribute(preset, "val"))) {
				return Color.BLACK;
			}
		}
		return defaultColor;
	}

	/**
	 * Return whether the pixel (x, y) of the tile of the supplied preset pattern is of the foreground color
	 */
	static boolean isForeground(String preset, int x, int y) {
		int down = Math.floorMod(x - y, SIZE);
		int up = Math.floorMod(x + y, SIZE);
		switch (preset) {
			case "horz":
				return y == 0;
			case "vert":
				return x == 0;
			case "ltHorz":
				return y % 4 == 0;
			case "ltVert":
				return x % 4 == 0;
			case "dkHorz":
				return y % 4 < 2;
			case "dkVert":
				return x % 4 < 2;
			case "narHorz":
				return y % 2 == 0;
			case "narVert":
				return x % 2 == 0;
			case "dashHorz":
				return y == 0 && x < 4 || y == 4 && x >= 4;
			case "dashVert":
				return x == 0 && y < 4 || x == 4 && y >= 4;
			case "cross":
			case "lgGrid":
				return x == 0 || y == 0;
			case "smGrid":
				return x % 4 == 0 || y % 4 == 0;
			case "dotGrid":
				return (x == 0 && y % 2 == 0) || (y == 0 && x % 2 == 0);
			case "dnDiag":
				return down % 4 == 0;
			case "ltDnDiag":
				return down % 4 == 0;
			case "dkDnDiag":
				return down % 4 < 2;
			case "wdDnDiag":
				return down < 3;
			case "dashDnDiag":
				return down % 4 == 0 && y % 4 < 2;
			case "upDiag":
			case "ltUpDiag":
				return up % 4 == 0;
			case "dkUpDiag":
				return up % 4 < 2;
			case "wdUpDiag":
				return up < 3;
			case "dashUpDiag":
				return up % 4 == 0 && y % 4 < 2;
			case "diagCross":
				return down % 4 == 0 || up % 4 == 0;
			case "openDmnd":
				return down == 0 || up == 0;
			case "smCheck":
				return (x / 2 + y / 2) % 2 == 0;
			case "lgCheck":
				return (x / 4 + y / 4) % 2 == 0;
			case "horzBrick":
				return y % 4 == 0 || (y < 4 ? x == 0 : x == 4);
			case "smConfetti":
			case "lgConfetti":
			case "sphere":
			case "weave":
			case "plaid":
			case "divot":
			case "shingle":
			case "wave":
			case "zigZag":
			case "trellis":
			case "solidDmnd":
			case "dotDmnd":
			case "diagBrick":
				// Not reproduced: an even mix of both colors
				return BAYER[y][x] < 32;
			default:
				if (preset.startsWith("pct")) {
					try {
						return BAYER[y][x] < Integer.parseInt(preset.substring(3)) * 64 / 100;
					} catch (NumberFormatException e) {
						return BAYER[y][x] < 32;
					}
				}
				return BAYER[y][x] < 32;
		}
	}
}
