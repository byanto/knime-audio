/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   May 23, 2016 (budiyanto): created
 */
package org.knime.audio.data.feature.mpeg7;

import org.knime.audio.data.feature.FeatureExtractor;
import org.knime.audio.util.MathUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.util.UniqueNameGenerator;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
public enum MPEG7FeatureType {
	/** Audio Waveform */
	AUDIO_WAVEFORM("Audio Waveform", "AudioWaveform", "Description AW", false, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			final double[] data = getAggregatedValues(aggregator, descriptors[0].getRaw());
			final DataCell[] cells = new DataCell[2];
			cells[MPEG7DocumentBuilder.MIN_IDX] = new DoubleCell(data[MPEG7DocumentBuilder.MIN_IDX]);
			cells[MPEG7DocumentBuilder.MAX_IDX] = new DoubleCell(data[MPEG7DocumentBuilder.MAX_IDX]);
			return cells;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			final UniqueNameGenerator generator = new UniqueNameGenerator(inSpec);
			return new DataColumnSpec[] { generator.newColumn(AUDIO_WAVEFORM.getName() + " - Min", DoubleCell.TYPE),
					generator.newColumn(AUDIO_WAVEFORM.getName() + " - Max", DoubleCell.TYPE) };
		}

	}),

	/** Audio Power */
	AUDIO_POWER("Audio Power", "AudioPower", "Description AP", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(AUDIO_POWER.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Audio Spectrum Envelope */
	AUDIO_SPECTRUM_ENVELOPE("Audio Spectrum Envelope", "AudioSpectrumEnvelope", "Description ASE", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(AUDIO_SPECTRUM_ENVELOPE.getName(),
							ListCell.getCollectionType(DoubleCell.TYPE)) };
		}

	}),

	/** Audio Spectrum Centroid / Spread */
	AUDIO_SPECTRUM_CENTROID_SPREAD("Audio Spectrum Centroid / Spread", "AudioSpectrumCentroidSpread", "Description Audio Spectrum Centroid / Spread", false, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			final double[] data = getAggregatedValues(aggregator, descriptors[0].getRaw());
			final DataCell[] cells = new DataCell[2];
			cells[MPEG7DocumentBuilder.CENTROID_IDX] = new DoubleCell(data[MPEG7DocumentBuilder.CENTROID_IDX]);
			cells[MPEG7DocumentBuilder.SPREAD_IDX] = new DoubleCell(data[MPEG7DocumentBuilder.SPREAD_IDX]);
			return cells;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			final UniqueNameGenerator generator = new UniqueNameGenerator(inSpec);
			return new DataColumnSpec[] {
					generator.newColumn("Audio Spectrum Centroid", DoubleCell.TYPE),
					generator.newColumn("Audio Spectrum Spread", DoubleCell.TYPE) };
		}

	}),

	/** Audio Spectrum Flatness */
	AUDIO_SPECTRUM_FLATNESS("Audio Spectrum Flatness", "AudioSpectrumFlatness", "Description ASF", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] { new UniqueNameGenerator(inSpec).newColumn(AUDIO_SPECTRUM_FLATNESS.getName(),
					ListCell.getCollectionType(DoubleCell.TYPE)) };
		}

	}),

	/** Audio Harmonicity */
	AUDIO_HARMONICITY("Audio Harmonicity", "AudioHarmonicity", "Description AH", false, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregatord,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			final UniqueNameGenerator generator = new UniqueNameGenerator(inSpec);
			return new DataColumnSpec[] {
					generator.newColumn(AUDIO_HARMONICITY.getName() + "- Harmonic Ratio", DoubleCell.TYPE),
					generator.newColumn(AUDIO_HARMONICITY.getName() + "- Upper Limit Of Harmonicity", DoubleCell.TYPE) };
		}

	}),

	/** Audio Fundamental Frequency */
	AUDIO_FUNDAMENTAL_FREQUENCY("Audio Fundamental Frequency", "AudioFundamentalFrequency", "Description AFF", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] { new UniqueNameGenerator(inSpec)
					.newColumn(AUDIO_FUNDAMENTAL_FREQUENCY.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Log Attack Time */
	LOG_ATTACK_TIME("Log Attack Time", "LogAttackTime", "Description LAT", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(LOG_ATTACK_TIME.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Temporal Centroid */
	TEMPORAL_CENTROID("Temporal Centroid", "TemporalCentroid", "Description TC", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(TEMPORAL_CENTROID.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Harmonic Spectral Centroid */
	HARMONIC_SPECTRAL_CENTROID("Harmonic Spectral Centroid", "HarmonicSpectralCentroid", "Description HSC", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(HARMONIC_SPECTRAL_CENTROID.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Harmonic Spectral Deviation */
	HARMONIC_SPECTRAL_DEVIATION("Harmonic Spectral Deviation", "HarmonicSpectralDeviation", "Description HSD", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(HARMONIC_SPECTRAL_DEVIATION.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Harmonic Spectral Spread */
	HARMONIC_SPECTRAL_SPREAD("Harmonic Spectral Spread", "HarmonicSpectralSpread", "Description HSS", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(HARMONIC_SPECTRAL_SPREAD.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Harmonic Spectral Variation */
	HARMONIC_SPECTRAL_VARIATION("Harmonic Spectral Variation", "HarmonicSpectralVariation", "Description HSV", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(HARMONIC_SPECTRAL_VARIATION.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Spectral Centroid */
	SPECTRAL_CENTROID("Spectral Centroid", "SpectralCentroid", "Description SC", false, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			return new DataColumnSpec[] {
					new UniqueNameGenerator(inSpec).newColumn(SPECTRAL_CENTROID.getName(), DoubleCell.TYPE) };
		}

	}),

	/** Audio Spectrum Basis / Projection */
	AUDIO_SPECTRUM_BASIS_PROJECTION("Audio Spectrum Basis / Projection", "AudioSpectrumBasisProjection", "Description Audio Spectrum Basis / Projection", true, new CellExtractor() {

		@Override
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
			final UniqueNameGenerator generator = new UniqueNameGenerator(inSpec);
			return new DataColumnSpec[] {
					generator.newColumn("Audio Spectrum Basis", ListCell.getCollectionType(DoubleCell.TYPE)),
					generator.newColumn("Audio Spectrum Projection", ListCell.getCollectionType(DoubleCell.TYPE)) };
		}

	});

	private final String m_name;
	private final String m_configName;
	private final String m_description;
	private final boolean m_hasParameters;
	private final CellExtractor m_cellExtractor;

	private MPEG7FeatureType(final String name, final String configName, final String description,
			final boolean hasParameters, final CellExtractor cellExtractor) {
		m_name = name;
		m_configName = configName;
		m_description = description;
		m_hasParameters = hasParameters;
		m_cellExtractor = cellExtractor;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the configName
	 */
	public String getConfigName() {
		return m_configName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * @return <code>true</code> if has parameters, otherwise <code>false</code>
	 */
	public boolean hasParameters() {
		return m_hasParameters;
	}

	public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec) {
		return m_cellExtractor.getColSpecs(inSpec);
	}

	public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
			final MPEG7AudioDescriptor... descriptors) {
		return m_cellExtractor.getCells(aggregator, descriptors);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getName();
	}

	private interface CellExtractor {
		/**
		 * @param audioDescriptor
		 *            the audio descriptor
		 * @return the extracted data as {@link DataCell}
		 */
		public DataCell[] getCells(final FeatureExtractor.Aggregator aggregator,
				final MPEG7AudioDescriptor... descriptors);

		/**
		 * @return the {@link DataType}
		 */
		public DataColumnSpec[] getColSpecs(final DataTableSpec inSpec);
	}

	private static double[] getAggregatedValues(final FeatureExtractor.Aggregator aggregator, final double[][] values) {
		if (aggregator == FeatureExtractor.Aggregator.MEAN) {
			return MathUtils.mean(values);
		} else if (aggregator == FeatureExtractor.Aggregator.STD_DEVIATION) {
			return MathUtils.standardDeviation(values);
		}
		return null;
	}

}
