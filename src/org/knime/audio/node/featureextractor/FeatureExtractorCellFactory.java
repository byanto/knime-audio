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
 *   Mar 24, 2016 (budiyanto): created
 */
package org.knime.audio.node.featureextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.knime.audio.data.Audio;
import org.knime.audio.data.AudioSamples;
import org.knime.audio.data.cell.AudioCell;
import org.knime.audio.data.cell.AudioValue;
import org.knime.audio.data.feature.FeatureExtractor;
import org.knime.audio.data.feature.FeatureType;
import org.knime.audio.util.AudioUtils;
import org.knime.audio.util.MathUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.node.NodeLogger;

/**
 * The {@link AbstractCellFactory} implementation of the AudioFeatureExtractor node
 * that creates a cell for each selected document property.
 *
 * @author Budi Yanto, KNIME.com
 */
class FeatureExtractorCellFactory extends AbstractCellFactory {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(
			FeatureExtractorCellFactory.class);

	private final int m_audioColIdx;
	private final FeatureExtractor[] m_extractors;
	private final int m_windowSizeInSamples;
	private final int m_windowsOverlapInPercent;
	private final FeatureExtractor.Aggregator[] m_aggregators;
	private final Set<FeatureExtractor> m_sortedExtractors;
	private final boolean m_firstDerivative;
	private final boolean m_secondDerivative;

	/**
	 *
	 * @param audioColIdx
	 * @param colSpecs
	 * @param extractors
	 * @param windowSizeInSamples
	 * @param windowsOverlapInPercent
	 * @param aggregators
	 */
	FeatureExtractorCellFactory(final int audioColIdx,
			final DataColumnSpec[] colSpecs, final FeatureExtractor[] extractors,
			final int windowSizeInSamples, final int windowsOverlapInPercent,
			final FeatureExtractor.Aggregator[] aggregators, final boolean firstDerivative,
			final boolean secondDerivative) {
		super(colSpecs);
		if (audioColIdx < 0) {
			throw new IllegalArgumentException("Invalid audio column");
		}

		if ((aggregators == null) || (aggregators.length < 1)) {
			throw new IllegalArgumentException("Aggregator method cannot be empty");
		}
		m_audioColIdx = audioColIdx;
		m_extractors = extractors;
		m_windowSizeInSamples = windowSizeInSamples;
		m_windowsOverlapInPercent = windowsOverlapInPercent;
		m_aggregators = aggregators;
		m_sortedExtractors = sortExtractors(extractors);
		m_firstDerivative = firstDerivative;
		m_secondDerivative = secondDerivative;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataCell[] getCells(final DataRow row) {
		final DataCell[] cells = new DataCell[getColumnSpecs().length];
		final DataCell cell = row.getCell(m_audioColIdx);
		if(!cell.getType().isCompatible(AudioValue.class)){
			throw new IllegalStateException("Invalid column type");
		}

		final Audio audio = ((AudioCell) cell).getAudio();

		try {
			final AudioSamples audioSamples = AudioUtils.getAudioSamples(audio);
			final int windowOverlapOffset = (int)((m_windowsOverlapInPercent / 100f)
					* m_windowSizeInSamples);

			/* Get chunk start indices */
			LOGGER.debug("Get chunk start indices");
			final List<Integer> chunkIndices = getChunkStartIndices(audioSamples,
					m_windowSizeInSamples, windowOverlapOffset);

			/* Extract features per chunk */
			LOGGER.debug("Extract features per chunk");
			final Map<FeatureType, List<double[]>> featuresPerChunk =
					extractFeaturesPerChunk(m_sortedExtractors, chunkIndices,
							audioSamples, m_windowSizeInSamples);

			Map<FeatureType, List<double[]>> firstDerivative = null;
			if (m_firstDerivative) {
				firstDerivative = getDerivative(featuresPerChunk);
			}

			Map<FeatureType, List<double[]>> secondDerivative = null;
			if (m_secondDerivative) {
				if (firstDerivative == null) {
					firstDerivative = getDerivative(featuresPerChunk);
				}
				secondDerivative = getDerivative(firstDerivative);
			}

			/* Put extracted features into DoubleCell */
			int cellIdx = 0;
			for (final FeatureExtractor extractor : m_extractors) {
				LOGGER.debug("Aggregate features");
				Map<FeatureExtractor.Aggregator, double[]> aggFeatures = aggregateFeatures(
						featuresPerChunk.get(extractor.getType()));

				cellIdx = buildCells(cellIdx, cells, extractor, aggFeatures);

				if (m_firstDerivative) {
					aggFeatures = aggregateFeatures(firstDerivative.get(extractor.getType()));
					cellIdx = buildCells(cellIdx, cells, extractor, aggFeatures);
				}

				if (m_secondDerivative) {
					aggFeatures = aggregateFeatures(secondDerivative.get(extractor.getType()));
					cellIdx = buildCells(cellIdx, cells, extractor, aggFeatures);
				}
				// for (final Entry<FeatureExtractor.Aggregator, double[]> entry
				// : aggFeatures.entrySet()) {
				// final DataCell[] featureCells =
				// extractor.getType().getDataCells(entry.getValue());
				// final int totalCells = featureCells.length;
				// LOGGER.debug("Put features of '" +
				// extractor.getType().getName() + "' into cell location " +
				// cellIdx
				// + " - " + ((cellIdx + totalCells) - 1));
				// System.arraycopy(featureCells, 0, cells, cellIdx,
				// totalCells);
				// cellIdx += totalCells;
				// }
			}
		} catch (final Exception ex) {
			LOGGER.error(ex);
		}

		return cells;
	}

	private int buildCells(final int currentCellIdx, final DataCell[] cells,
			final FeatureExtractor extractor, final Map<FeatureExtractor.Aggregator, double[]> aggFeatures) {
		int cellIdx = currentCellIdx;
		for (final Entry<FeatureExtractor.Aggregator, double[]> entry : aggFeatures.entrySet()) {
			final DataCell[] featureCells = extractor.getType().getDataCells(entry.getValue());
			final int totalCells = featureCells.length;
			LOGGER.debug("Put features of '" + extractor.getType().getName() + "' into cell location " + cellIdx + " - "
					+ ((currentCellIdx + totalCells) - 1));
			System.arraycopy(featureCells, 0, cells, cellIdx, totalCells);
			cellIdx += totalCells;
		}
		return cellIdx;
	}

	private Map<FeatureType, List<double[]>> getDerivative(final Map<FeatureType, List<double[]>> data) {
		final Map<FeatureType, List<double[]>> result = new HashMap<FeatureType, List<double[]>>();
		for (final Entry<FeatureType, List<double[]>> entry : data.entrySet()) {
			result.put(entry.getKey(), MathUtils.derivative(entry.getValue()));
		}
		return result;
	}

	private Set<FeatureExtractor> sortExtractors(final FeatureExtractor[] extractors) {
		LOGGER.debug("Sort the feature extractors");
		final Set<FeatureExtractor> result = new LinkedHashSet<FeatureExtractor>();
		final Map<FeatureType, FeatureExtractor> temp = new HashMap<FeatureType, FeatureExtractor>();

		final Set<FeatureType> orderedTypes = new LinkedHashSet<FeatureType>();
		for (final FeatureExtractor ext : extractors) {
			temp.put(ext.getType(), ext);
			sortFeatureType(ext.getType(), orderedTypes);
		}

		for (final FeatureType type : orderedTypes) {
			if (temp.containsKey(type)) {
				result.add(temp.get(type));
			} else {
				result.add(FeatureExtractor.getFeatureExtractor(type));
			}
		}

		return result;
	}

	private static void sortFeatureType(final FeatureType type, final Set<FeatureType> set) {
		for (final FeatureType ft : type.getDependencies()) {
			sortFeatureType(ft, set);
		}
		set.add(type);
	}

	private List<Integer> getChunkStartIndices(final AudioSamples wholeSamples, final int chunkSize,
			final int chunkOverlapOffset) {
		final double[] samples = wholeSamples.getSamplesMixedDownIntoOneChannel();
		final List<Integer> result = new ArrayList<Integer>();
		int position = 0;
		while (position < samples.length) {
			position -= chunkOverlapOffset;
			if (position < 0) {
				position = 0;
			}
			result.add(position);
			position += chunkSize;
		}
		return result;
	}

	private Map<FeatureType, List<double[]>> extractFeaturesPerChunk(final Set<FeatureExtractor> sortedExtractors,
			final List<Integer> chunkIndices, final AudioSamples wholeSamples, final int chunkSize) throws Exception {

		final Map<FeatureType, List<double[]>> result = new HashMap<FeatureType, List<double[]>>();
		final double[] samples = wholeSamples.getSamplesMixedDownIntoOneChannel();
		int toRead = chunkSize;
		for (int i = 0; i < chunkIndices.size(); i++) {
			if (i == (chunkIndices.size() - 1)) {
				/* Last index, only read the rest of the samples */
				toRead = samples.length - chunkIndices.get(i);
			}
			final double[] buf = new double[chunkSize];
			System.arraycopy(samples, chunkIndices.get(i), buf, 0, toRead);
			final AudioSamples chunk = new AudioSamples(buf, wholeSamples.getAudioFormat());
			for (final FeatureExtractor extractor : sortedExtractors) {
				final FeatureType type = extractor.getType();
				final FeatureType[] dependencies = type.getDependencies();
				double[][] additionalValues = new double[dependencies.length][];
				for (int j = 0; j < dependencies.length; j++) {
					additionalValues[j] = result.get(dependencies[j]).get(i);
				}
				if (!type.hasDependencies()) {
					additionalValues = null;
				}
				final double[] features = extractor.extractFeature(chunk, additionalValues);
				List<double[]> list = result.get(type);
				if (list == null) {
					list = new ArrayList<double[]>();
					list.add(features);
					result.put(type, list);
				} else {
					list.add(features);
				}
			}
		}

		return result;
	}

	private Map<FeatureExtractor.Aggregator, double[]> aggregateFeatures(
			final List<double[]> featuresPerChunk) {
		final Map<FeatureExtractor.Aggregator, double[]> result = new LinkedHashMap<FeatureExtractor.Aggregator, double[]>();
		final double[][] features = featuresPerChunk.toArray(new double[featuresPerChunk.size()][]);
		final RealMatrix rm = MatrixUtils.createRealMatrix(features);
		for (final FeatureExtractor.Aggregator aggregator : m_aggregators) {
			final double[] aggregationValues = new double[rm.getColumnDimension()];
			for (int i = 0; i < aggregationValues.length; i++) {
				switch (aggregator) {
				case MEAN:
					aggregationValues[i] = StatUtils.mean(rm.getColumn(i));
					break;
				case STD_DEVIATION:
					aggregationValues[i] = Math.sqrt(StatUtils.variance(rm.getColumn(i)));
					break;
				case MAX:
					aggregationValues[i] = StatUtils.max(rm.getColumn(i));
					break;
				case MIN:
					aggregationValues[i] = StatUtils.min(rm.getColumn(i));
					break;
				case VARIANCE:
					aggregationValues[i] = StatUtils.variance(rm.getColumn(i));
					break;
				default:
					aggregationValues[i] = 0;
					break;
				}
			}
			result.put(aggregator, aggregationValues);
		}
		return result;
	}

}
