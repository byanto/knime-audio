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
 *   Jun 6, 2016 (budiyanto): created
 */
package org.knime.audio.node.mpeg7featureextractor;

import java.io.File;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.knime.audio.data.cell.AudioCell;
import org.knime.audio.data.cell.AudioValue;
import org.knime.audio.data.feature.FeatureExtractor;
import org.knime.audio.data.feature.mpeg7.MPEG7AudioDescriptor;
import org.knime.audio.data.feature.mpeg7.MPEG7DocumentBuilder;
import org.knime.audio.data.feature.mpeg7.MPEG7FeatureType;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.node.NodeLogger;

import de.crysandt.audio.mpeg7audio.Config;

/**
 *
 * @author Budi Yanto, Berlin, KNIME.com
 */
public class MPEG7FeatureExtractorCellFactory extends AbstractCellFactory {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(MPEG7FeatureExtractorCellFactory.class);
	private final int m_audioColIdx;
	private final FeatureExtractor.Aggregator m_aggregator;
	private final Config m_config;
	private final MPEG7FeatureType[] m_selectedFeatures;

	MPEG7FeatureExtractorCellFactory(final int audioColIdx, final String aggregator, final Config config,
			final MPEG7FeatureType[] selectedFeatures, final DataColumnSpec[] colSpecs) {
		super(colSpecs);
		m_audioColIdx = audioColIdx;
		m_aggregator = FeatureExtractor.getAggregator(aggregator);
		m_config = config;
		m_selectedFeatures = selectedFeatures;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataCell[] getCells(final DataRow row) {
		final DataCell[] cells = new DataCell[getColumnSpecs().length];
		final DataCell cell = row.getCell(m_audioColIdx);
		if (!cell.getType().isCompatible(AudioValue.class)) {
			throw new IllegalStateException("Invalid column type");
		}

		final File file = ((AudioCell) cell).getAudio().getFile();
		try{
			final AudioInputStream ais = AudioSystem.getAudioInputStream(file);
			final MPEG7DocumentBuilder builder = new MPEG7DocumentBuilder();
			builder.encode(ais, m_config, false);
			final Map<String, MPEG7AudioDescriptor> descriptors = builder.getDescriptors();
			int cellIdx = 0;
			for (final MPEG7FeatureType type : m_selectedFeatures) {
				final String key = type.getConfigName();
				final MPEG7AudioDescriptor desc = descriptors.get(key);
				final DataCell[] featureCells = type.getCells(m_aggregator, desc);
				final int totalCells = featureCells.length;
				System.arraycopy(featureCells, 0, cells, cellIdx, totalCells);
				cellIdx += totalCells;
			}

		} catch (final Exception ex) {
			LOGGER.error(ex);
		}

		return cells;
	}

	// private double[] getAggregatedValues(final double[][] values) {
	// if (m_aggregator.equals(FeatureExtractor.Aggregator.MEAN)) {
	// return MathUtils.mean(values);
	// } else if
	// (m_aggregator.equals(FeatureExtractor.Aggregator.STD_DEVIATION)) {
	// return MathUtils.standardDeviation(values);
	// }
	// return null;
	// }
	//
	// private int copyToResultCells(final DataCell[] newCells, final DataCell[]
	// resultCells, final int currentIdx) {
	// final int totalCells = newCells.length;
	// System.arraycopy(newCells, 0, resultCells, currentIdx, totalCells);
	// return currentIdx + totalCells;
	// }

}
