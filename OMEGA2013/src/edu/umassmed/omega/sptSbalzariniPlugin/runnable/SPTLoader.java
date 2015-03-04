/*******************************************************************************
 * Copyright (C) 2014 University of Massachusetts Medical School
 * Alessandro Rigano (Program in Molecular Medicine)
 * Caterina Strambio De Castillia (Program in Molecular Medicine)
 *
 * Created by the Open Microscopy Environment inteGrated Analysis (OMEGA) team: 
 * Alex Rigano, Caterina Strambio De Castillia, Jasmine Clark, Vanni Galli, 
 * Raffaello Giulietti, Loris Grossi, Eric Hunter, Tiziano Leidi, Jeremy Luban, 
 * Ivo Sbalzarini and Mario Valle.
 *
 * Key contacts:
 * Caterina Strambio De Castillia: caterina.strambio@umassmed.edu
 * Alex Rigano: alex.rigano@umassmed.edu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package edu.umassmed.omega.sptSbalzariniPlugin.runnable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.umassmed.omega.commons.constants.OmegaConstants;
import edu.umassmed.omega.commons.constants.OmegaConstantsError;
import edu.umassmed.omega.commons.gui.interfaces.OmegaMessageDisplayerPanelInterface;
import edu.umassmed.omega.core.OmegaLogFileManager;
import edu.umassmed.omega.data.coreElements.OmegaFrame;
import edu.umassmed.omega.data.coreElements.OmegaImage;
import edu.umassmed.omega.data.coreElements.OmegaImagePixels;
import edu.umassmed.omega.data.imageDBConnectionElements.OmegaGateway;

public class SPTLoader implements SPTRunnable {
	private static final String RUNNER = "Loader service: ";
	private final OmegaMessageDisplayerPanelInterface displayerPanel;
	private final OmegaImage image;
	private final OmegaGateway gateway;
	private final int z, c;

	private boolean isJobCompleted, isKilled;

	public SPTLoader(final OmegaMessageDisplayerPanelInterface displayerPanel,
	        final OmegaImage image, final int z, final int c,
	        final OmegaGateway gateway) {
		this.displayerPanel = displayerPanel;
		this.image = image;
		this.z = z;
		this.c = c;

		this.gateway = gateway;

		this.isJobCompleted = false;
	}

	@Override
	public boolean isJobCompleted() {
		return this.isJobCompleted;
	}

	@Override
	public void run() {
		this.updateStatusSync(
		        SPTLoader.RUNNER + " started image " + this.image.getName(),
		        false);
		final OmegaImagePixels defaultPixels = this.image.getDefaultPixels();
		// ID of the pixels
		final Long pixelsID = defaultPixels.getElementID();
		// number of frames for this image
		final int framesNumber = defaultPixels.getSizeT();
		// number of bytes of this image
		final int byteWidth = this.gateway.getByteWidth(pixelsID);

		boolean error = false;

		// byte[] oldPixels = null;

		for (int t = 0; t < framesNumber; t++) {
			if (this.isKilled) {
				break;
			}
			final int frameIndex = t + 1;
			this.updateStatusSync(
			        SPTLoader.RUNNER + " image " + this.image.getName()
			                + ", frame(s) " + frameIndex + "/" + framesNumber,
			        false);
			final List<OmegaFrame> frames = defaultPixels.getFrames(this.c,
			        this.z);
			OmegaFrame frame = null;
			if (!frames.isEmpty() && (frames.size() > t)) {
				frame = frames.get(t);
			} else {
				frame = new OmegaFrame(t, this.c, this.z);
				frame.setParentPixels(defaultPixels);
				defaultPixels.addFrame(this.c, this.z, frame);
			}
			// TODO update panel with loading frame number
			// JPanelSPT.this.jLabelStatusDetails.setText(String.format(
			// "loading frame %d / %d", i + 1, framesNumber));

			try {
				final byte[] pixels = this.gateway.getImageData(pixelsID,
				        this.z, t, this.c);
				// if (oldPixels != null) {
				// boolean tof = true;
				// for (int i = 0; i < pixels.length; i++)
				// if (oldPixels[i] != pixels[i]) {
				// tof = false;
				// break;
				// }
				// if (tof) {
				// System.out.println("data in " + (t - 1)
				// + " == data in " + t);
				// }
				// }
				//
				// oldPixels = pixels;

				int[] data = null;

				// Manage the right amount of byte per pixels
				switch (byteWidth) {
				case 1:
					// 8 bit image
					System.out.println("Loading t: " + t + " 8 bit");
					data = new int[pixels.length];
					for (int j = 0; j < data.length; j++) {
						final int b0 = pixels[j] & 0xff;
						data[j] = b0 << 0;
					}
					break;
				case 2:
					// 16 bit image
					System.out.println("Loading t: " + t + " 16 bit");
					data = new int[pixels.length / 2];
					for (int j = 0; j < data.length; j++) {
						final int b0 = pixels[2 * j] & 0xff;
						final int b1 = pixels[(2 * j) + 1] & 0xff;
						data[j] = (b0 << 8) | (b1 << 0);
					}
					break;
				case 3:
					// 24 bit image
					System.out.println("Loading t: " + t + " 24 bit");
					data = new int[pixels.length / 3];
					for (int j = 0; j < data.length; j++) {
						final int b0 = pixels[3 * j] & 0xff;
						final int b1 = pixels[(3 * j) + 1] & 0xff;
						final int b2 = pixels[(3 * j) + 2] & 0xff;
						data[j] = (b0 << 16) | (b1 << 8) | (b2 << 0);
					}
					break;
				case 4:
					// 32 bit image
					System.out.println("Loading t: " + t + " 32 bit");
					data = new int[pixels.length / 4];
					for (int j = 0; j < data.length; j++) {
						final int b0 = pixels[4 * j] & 0xff;
						final int b1 = pixels[(4 * j) + 1] & 0xff;
						final int b2 = pixels[(4 * j) + 2] & 0xff;
						final int b3 = pixels[(4 * j) + 3] & 0xff;
						data[j] = (b0 << 24) | (b1 << 16) | (b2 << 8)
						        | (b3 << 0);
					}
					break;
				}
				SPTDLLInvoker.callLoadImage(data);
			} catch (final Exception ex) {
				error = true;
				OmegaLogFileManager.handleUncaughtException(ex);
			}
		}
		if (this.isKilled)
			return;

		if (error) {
			JOptionPane.showMessageDialog(null,
			        OmegaConstantsError.ERROR_DURING_SPT_RUN,
			        OmegaConstants.OMEGA_TITLE, JOptionPane.ERROR_MESSAGE);
		}

		this.updateStatusAsync(SPTLoader.RUNNER + " ended.", true);
		this.isJobCompleted = true;
	}

	private void updateStatusSync(final String msg, final boolean ended) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					SPTLoader.this.displayerPanel
					        .updateMessageStatus(new SPTMessageEvent(msg,
					                SPTLoader.this, ended));
				}
			});
		} catch (final InvocationTargetException | InterruptedException ex) {
			OmegaLogFileManager.handleUncaughtException(ex);
		}
	}

	private void updateStatusAsync(final String msg, final boolean ended) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SPTLoader.this.displayerPanel
				        .updateMessageStatus(new SPTMessageEvent(msg,
				                SPTLoader.this, ended));
			}
		});
	}

	public void kill() {
		this.isKilled = true;
	}
}