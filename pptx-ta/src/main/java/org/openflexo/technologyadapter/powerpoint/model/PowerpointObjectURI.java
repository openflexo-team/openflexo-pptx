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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds and resolves the URIs of the objects of a presentation (see {@link PowerpointObject#getUri()}).<br>
 * A URI is the URI of the presentation resource, then <code>#</code>, then a path from the presentation to the object:
 *
 * <pre>
 * &lt;resource URI&gt;#/slide[id=258]/shape[id=12]/shape[id=14]/paragraph[1]
 * &lt;resource URI&gt;#/slide[id=263]/shape[id=25,n=2]
 * </pre>
 *
 * <ul>
 * <li>a slide is designated by the <code>id</code> of its <code>p:sldId</code> in <code>presentation.xml</code>, which does not change
 * when slides are reordered;</li>
 * <li>a shape by its <code>cNvPr id</code>, under its group for a nested shape. The format forbids two shapes of a slide sharing an id,
 * but some files do (PPTX-D-1): <code>n</code> then gives the rank of the shape among its siblings sharing that id, in document order (1,
 * omitted, for the first one);</li>
 * <li>the rows and cells of a table, the paragraphs of a text and the series of a chart have no identifier in the file: they are
 * designated by their index in their shape.</li>
 * </ul>
 * The positional URIs written before (<code>&lt;resource URI&gt;/slide[3]/shape[2]</code>, where 3 and 2 are indexes) are still resolved.
 *
 * @author sylvain
 *
 */
public class PowerpointObjectURI {

	private static final Logger logger = Logger.getLogger(PowerpointObjectURI.class.getPackage().getName());

	/** Separates the URI of the presentation resource from the path of an object in it */
	public static final String PATH_SEPARATOR = "#";

	public static final String SLIDE = "slide";
	public static final String SHAPE = "shape";
	public static final String ROW = "row";
	public static final String CELL = "cell";
	public static final String PARAGRAPH = "paragraph";
	public static final String SERIES = "series";

	private static final Pattern SEGMENT = Pattern.compile("(\\w+)\\[(?:id=(-?\\d+)(?:,n=(\\d+))?|(-?\\d+))\\]");

	private PowerpointObjectURI() {
	}

	/**
	 * Return the path segment designating supplied slide
	 */
	public static String slideSegment(PowerpointSlide slide) {
		return "/" + SLIDE + "[id=" + slide.getSlideId() + "]";
	}

	/**
	 * Return the path segment designating supplied shape among supplied siblings (the shapes of its slide or of its group), or among no
	 * siblings when null
	 */
	public static String shapeSegment(PowerpointShape shape, List<PowerpointShape> siblings) {
		int id = shape.getShapeId();
		int rank = 1;
		if (siblings != null) {
			for (PowerpointShape sibling : siblings) {
				if (sibling == shape) {
					break;
				}
				if (sibling.getShapeId() == id) {
					rank++;
				}
			}
		}
		return "/" + SHAPE + "[id=" + id + (rank > 1 ? ",n=" + rank : "") + "]";
	}

	/**
	 * Return the object of supplied presentation designated by supplied URI, or null when there is none.<br>
	 * Only the path of the URI is used: the presentation is the one supplied, whatever the resource URI the path follows.
	 *
	 * @param slideshow
	 * @param uri
	 *            a URI built by {@link PowerpointObject#getUri()}, or a positional URI written before
	 * @return
	 */
	public static PowerpointObject resolve(PowerpointSlideshow slideshow, String uri) {
		if (slideshow == null || uri == null) {
			return null;
		}
		String path;
		int separator = uri.lastIndexOf(PATH_SEPARATOR);
		if (separator > -1) {
			path = uri.substring(separator + 1);
		}
		else {
			// A positional URI: <resource URI>/slide[3]/shape[2]
			int slideIndex = uri.indexOf("/" + SLIDE + "[");
			if (slideIndex == -1) {
				return uri.equals(slideshow.getUri()) ? slideshow : null;
			}
			path = uri.substring(slideIndex);
		}
		PowerpointObject current = slideshow;
		for (String segment : path.split("/")) {
			if (segment.isEmpty()) {
				continue;
			}
			Matcher matcher = SEGMENT.matcher(segment);
			if (!matcher.matches()) {
				logger.warning("Malformed segment " + segment + " in PowerPoint URI " + uri);
				return null;
			}
			current = resolveSegment(current, matcher.group(1), matcher.group(2) != null ? Long.parseLong(matcher.group(2)) : null,
					matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 1,
					matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : -1);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	/**
	 * Return the child of supplied object designated by a segment: either by an identifier (and the rank among those sharing it), or by an
	 * index when the identifier is null
	 */
	private static PowerpointObject resolveSegment(PowerpointObject parent, String kind, Long id, int rank, int index) {
		switch (kind) {
			case SLIDE:
				if (parent instanceof PowerpointSlideshow) {
					List<PowerpointSlide> slides = ((PowerpointSlideshow) parent).getPowerpointSlides();
					if (id == null) {
						return get(slides, index);
					}
					for (PowerpointSlide slide : slides) {
						if (slide.getSlideId() == id) {
							return slide;
						}
					}
				}
				return null;
			case SHAPE:
				List<PowerpointShape> shapes;
				if (parent instanceof PowerpointSlide) {
					shapes = ((PowerpointSlide) parent).getPowerpointShapes();
				}
				else if (parent instanceof PowerpointShapeGroup) {
					shapes = ((PowerpointShapeGroup) parent).getChildren();
				}
				else {
					return null;
				}
				if (id == null) {
					return get(shapes, index);
				}
				int found = 0;
				for (PowerpointShape shape : shapes) {
					if (shape.getShapeId() == id && ++found == rank) {
						return shape;
					}
				}
				// The shape was moved into or out of a group: an identifier shared by no other shape still designates it
				if (rank == 1 && parent instanceof PowerpointSlideElement) {
					PowerpointSlide slide = parent instanceof PowerpointSlide ? (PowerpointSlide) parent
							: ((PowerpointSlideElement) parent).getPowerpointSlide();
					return slide != null ? slide.getShapeWithId(id.intValue()) : null;
				}
				return null;
			case ROW:
				return parent instanceof PowerpointTable ? get(((PowerpointTable) parent).getRows(), index) : null;
			case CELL:
				return parent instanceof PowerpointTableRow ? get(((PowerpointTableRow) parent).getCells(), index) : null;
			case PARAGRAPH:
				return parent instanceof PowerpointTextShape ? get(((PowerpointTextShape) parent).getParagraphs(), index) : null;
			case SERIES:
				return parent instanceof PowerpointChart ? get(((PowerpointChart) parent).getSeries(), index) : null;
			default:
				logger.warning("Unknown segment " + kind + " in a PowerPoint URI");
				return null;
		}
	}

	private static <T extends PowerpointObject> T get(List<T> list, int index) {
		return index >= 0 && index < list.size() ? list.get(index) : null;
	}

}
