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

package org.openflexo.technologyadapter.powerpoint.rm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FileWritingLock;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.StreamIODelegate;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointModelFactory;
import org.openflexo.technologyadapter.powerpoint.model.PowerpointSlideshow;
import org.openflexo.technologyadapter.powerpoint.model.io.BasicPowerpointModelConverter;
import org.openflexo.toolbox.FileUtils;

/**
 * Represents the resource associated to a {@link PowerpointSlideshow}
 *
 * @author sylvain
 *
 */
public abstract class PowerpointSlideshowResourceImpl extends PamelaResourceImpl<PowerpointSlideshow, PowerpointModelFactory>
		implements PowerpointSlideshowResource {

	private static final Logger logger = Logger.getLogger(PowerpointSlideshowResourceImpl.class.getPackage().getName());

	private BasicPowerpointModelConverter converter;

	public PowerpointSlideshowResourceImpl() {
	}

	@Override
	public BasicPowerpointModelConverter getConverter() {
		if (converter == null) {
			converter = new BasicPowerpointModelConverter(this);
		}
		return converter;
	}

	@Override
	protected PowerpointSlideshow performLoad() throws IOException, Exception {

		converter = new BasicPowerpointModelConverter(this);

		if (getFlexoIOStreamDelegate() == null) {
			throw new IOFlexoException("Cannot load PowerPoint document with this IO/delegate: " + getIODelegate());
		}

		notifyResourceWillLoad();

		PowerpointSlideshow returned = null;
		try {
			returned = createOrLoadPowerpointSlideshow(getFlexoIOStreamDelegate());
		} catch (OfficeXmlFileException e) {
			throw new IOFlexoException(e.getMessage());
		}

		if (returned == null) {
			logger.warning("Cannot retrieve resource data from serialization artifact " + getIODelegate().toString());
			return null;
		}

		notifyResourceLoaded();

		return returned;
	}

	@Override
	public void unloadResourceData(boolean deleteResourceData) {
		super.unloadResourceData(deleteResourceData);
		if (converter != null) {
			converter.delete();
		}
		converter = null;
	}

	@Override
	public <I> PowerpointSlideshow createOrLoadPowerpointSlideshow(StreamIODelegate<I> ioDelegate) {
		XMLSlideShow slideShow = null;
		try {
			if (!ioDelegate.exists()) {
				// The serialization artefact does not exist yet: create an empty presentation
				slideShow = new XMLSlideShow();
			}
			else {
				slideShow = new XMLSlideShow(ioDelegate.getInputStream());
			}
			return getConverter().convertPowerpointSlideshow(slideShow);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Cannot read PowerPoint document " + getIODelegate(), e);
			return null;
		}
	}

	@Override
	public PowerpointSlideshow getPowerpointSlideshow() {
		try {
			return getResourceData();
		} catch (ResourceLoadingCancelledException | FileNotFoundException | FlexoException e) {
			logger.log(Level.WARNING, "Cannot access PowerPoint slideshow resource data", e);
			return null;
		}
	}

	@Override
	public Class<PowerpointSlideshow> getResourceDataClass() {
		return PowerpointSlideshow.class;
	}

	/**
	 * Write file.
	 *
	 * @throws SaveResourceException
	 */
	private void write(OutputStream out) throws SaveResourceException {
		logger.info("Writing " + getIODelegate().getSerializationArtefact());
		try {
			getPowerpointSlideshow().getSlideShow().write(out);
		} catch (IOException e) {
			throw new SaveResourceException(getIODelegate(), e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				logger.log(Level.WARNING, "Cannot close output stream", e);
			}
		}
		logger.info("Wrote " + getIODelegate().getSerializationArtefact());
	}

	@Override
	protected void performSave(boolean clearIsModified) throws SaveResourceException {

		if (getFlexoIOStreamDelegate() == null) {
			throw new SaveResourceException(getIODelegate());
		}

		FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

		if (logger.isLoggable(Level.INFO)) {
			logger.info("Saving resource " + this + " : " + getIODelegate().getSerializationArtefact());
		}

		if (getFlexoIOStreamDelegate() instanceof FileIODelegate) {
			File temporaryFile = null;
			try {
				File fileToSave = ((FileIODelegate) getFlexoIOStreamDelegate()).getFile();
				// Make local copy
				makeLocalCopy(fileToSave);
				// Using temporary file
				temporaryFile = ((FileIODelegate) getIODelegate()).createTemporaryArtefact(".pptx");
				if (logger.isLoggable(Level.FINE)) {
					logger.finer("Creating temp file " + temporaryFile.getAbsolutePath());
				}
				try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
					write(fos);
				}
				if (logger.isLoggable(Level.FINE)) {
					logger.finer("Renamed " + temporaryFile + " to " + fileToSave);
				}
				FileUtils.rename(temporaryFile, fileToSave);
			} catch (IOException e) {
				if (temporaryFile != null) {
					temporaryFile.delete();
				}
				logger.log(Level.WARNING, "Failed to save resource " + getIODelegate().getSerializationArtefact(), e);
				getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
				throw new SaveResourceException(getIODelegate(), e);
			}
		}
		else {
			write(getOutputStream());
		}

		getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
		if (clearIsModified) {
			notifyResourceStatusChanged();
		}
	}

}
