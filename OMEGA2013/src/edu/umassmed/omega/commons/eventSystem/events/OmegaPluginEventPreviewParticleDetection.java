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
package edu.umassmed.omega.commons.eventSystem.events;

import java.util.List;

import edu.umassmed.omega.commons.plugins.OmegaPlugin;
import edu.umassmed.omega.data.analysisRunElements.OmegaParameter;
import edu.umassmed.omega.data.coreElements.OmegaElement;
import edu.umassmed.omega.data.coreElements.OmegaFrame;
import edu.umassmed.omega.data.trajectoryElements.OmegaROI;

public class OmegaPluginEventPreviewParticleDetection extends
        OmegaPluginEventAlgorithm {

	private final OmegaFrame frame;
	private final List<OmegaROI> resultingParticles;

	public OmegaPluginEventPreviewParticleDetection(final OmegaElement element,
	        final List<OmegaParameter> params, final OmegaFrame frame,
	        final List<OmegaROI> resultingParticles) {
		this(null, element, params, frame, resultingParticles);
	}

	public OmegaPluginEventPreviewParticleDetection(final OmegaPlugin source,
	        final OmegaElement element, final List<OmegaParameter> params,
	        final OmegaFrame frame, final List<OmegaROI> resultingParticles) {
		super(source, element, params);

		this.frame = frame;
		this.resultingParticles = resultingParticles;
	}

	public OmegaFrame getFrame() {
		return this.frame;
	}

	public List<OmegaROI> getResultingParticles() {
		return this.resultingParticles;
	}
}
