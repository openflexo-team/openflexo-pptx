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

package org.openflexo.technologyadapter.powerpoint;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.technologyadapter.powerpoint.model.PowerpointObject;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointObjectURI;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;

/**
 * Computes the URIs stored by the actor references of a {@link BasicPowerpointModelSlot}, and finds an object again from its URI.<br>
 * A URI is the URI of the object (see {@link PowerpointObjectURI}), URL-encoded.
 * 
 * @author sylvain
 *
 */
public class BasicPowerpointModelSlotURIProcessor {

	private static final Logger logger = Logger.getLogger(BasicPowerpointModelSlotURIProcessor.class.getPackage().getName());

	// An optimization only: an object is always found again from its URI, see retrieveObjectWithURI()
	private final Map<String, PowerpointObject> uriCache = new HashMap<>();

	public String getURIForObject(PowerpointSlideshow resourceData, PowerpointObject powerpointObject) {
		try {
			String builtURI = URLEncoder.encode(powerpointObject.getUri(), "UTF-8");
			uriCache.put(builtURI, powerpointObject);
			return builtURI;
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "Cannot process URI - Unexpected encoding error", e);
			return null;
		}
	}

	/**
	 * Return the object designated by supplied URI in supplied presentation.<br>
	 * The URI is resolved by navigating the presentation (see {@link PowerpointObjectURI}), so that it is found again after a reload. A
	 * cached object is only returned when it still belongs to supplied presentation: after a reload, the cache holds the objects of the
	 * former one.
	 */
	public PowerpointObject retrieveObjectWithURI(PowerpointSlideshow resourceData, String objectURI) throws UnsupportedEncodingException {
		if (objectURI == null) {
			return null;
		}
		PowerpointObject cached = uriCache.get(objectURI);
		if (cached != null && !cached.isDeleted() && cached.getResourceData() == resourceData) {
			return cached;
		}
		PowerpointObject returned = PowerpointObjectURI.resolve(resourceData, URLDecoder.decode(objectURI, "UTF-8"));
		if (returned != null) {
			uriCache.put(objectURI, returned);
		}
		return returned;
	}

}
