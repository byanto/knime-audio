package org.knime.audio.node.featureextractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.audio.data.feature.FeatureExtractor;
import org.knime.audio.data.feature.FeatureType;
import org.knime.audio.dialogcomponent.AudioColumnSelection;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.util.UniqueNameGenerator;

/**
 * This is the model implementation of FeatureExtractor.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class FeatureExtractorNodeModel extends NodeModel {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(FeatureExtractorNodeModel.class);

	static final int DEF_WINDOW_SIZE = 512;
	static final int DEF_WINDOW_OVERLAP = 0;

	static final String MEAN = "Mean";
	static final String STD_DEVIATION = "Standard Deviation";

	private final AudioColumnSelection m_audioColumnSelectionSettingsModel = new AudioColumnSelection();
	private final SettingsModelIntegerBounded m_windowSizeSettingsModel = createWindowSizeSettingsModel();
	private final SettingsModelIntegerBounded m_windowOverlapSettingsModel = createWindowOverlapSettingsModel();
	private final SettingsModelBoolean[] m_aggregatorSettingsModel = createAggregatorSettingsModel();
	private final SettingsModelBoolean m_firstDerivativeModel = createFirstDerivativeSettingsModel();
	private final SettingsModelBoolean m_secondDerivativeModel = createSecondDerivativeSettingsModel();

	private final FeatureExtractorSettings m_settings = new FeatureExtractorSettings();

	static SettingsModelIntegerBounded createWindowSizeSettingsModel() {
		return new SettingsModelIntegerBounded("window_size", DEF_WINDOW_SIZE, 4, Integer.MAX_VALUE);
	}

	static SettingsModelIntegerBounded createWindowOverlapSettingsModel() {
		return new SettingsModelIntegerBounded("window_overlap", DEF_WINDOW_OVERLAP, 0, 99);
	}

	// static SettingsModelString createAggregatorSettingsModel() {
	// return new SettingsModelString("aggregator", MEAN);
	// }

	static SettingsModelBoolean[] createAggregatorSettingsModel() {
		final FeatureExtractor.Aggregator[] aggs = FeatureExtractor.Aggregator.values();
		final SettingsModelBoolean[] models = new SettingsModelBoolean[aggs.length];
		for (int i = 0; i < aggs.length; i++) {
			models[i] = new SettingsModelBoolean(aggs[i].getName(), aggs[i].getDefaultValue());
		}
		return models;
	}

	static SettingsModelBoolean createFirstDerivativeSettingsModel() {
		return new SettingsModelBoolean("first_derivative", false);
	}

	static SettingsModelBoolean createSecondDerivativeSettingsModel() {
		return new SettingsModelBoolean("second_derivative", false);
	}

	/**
	 * Constructor for the node model.
	 */
	protected FeatureExtractorNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		LOGGER.debug("--------------------------");
		LOGGER.debug("Audio Column: " + m_audioColumnSelectionSettingsModel.getSelectedColumn());
		LOGGER.debug("Windows size: " + m_windowSizeSettingsModel.getIntValue());
		LOGGER.debug("Window overlap: " + m_windowOverlapSettingsModel.getIntValue());
		for (final SettingsModelBoolean model : m_aggregatorSettingsModel) {
			LOGGER.debug("Aggregator: " + model.getConfigName() + " - " + model.getBooleanValue());
		}
		LOGGER.debug("--------------------------");
		LOGGER.debug("All Features");
		for(final FeatureType type : m_settings.getAudioFeatureTypes()){
			LOGGER.debug("Type: " + type.getName());
			LOGGER.debug("Is Selected: " + m_settings.isSelected(type));
			for(final String parameter : type.getParameters()){
				LOGGER.debug("Parameter: " + parameter);
				LOGGER.debug("Parameter Value: " + m_settings.getParameterValue(type, parameter));
			}
		}
		LOGGER.debug("--------------------------");
		LOGGER.debug("Selected Features");
		for(final FeatureType type : m_settings.getSelectedFeatures()){
			LOGGER.debug("Type: " + type.getName());
			LOGGER.debug("Is Selected: " + m_settings.isSelected(type));
			for(final String parameter : type.getParameters()){
				LOGGER.debug("Parameter: " + parameter);
				LOGGER.debug("Parameter Value: " + m_settings.getParameterValue(type, parameter));
			}
		}

		if ((inData == null) || (inData.length < 1)) {
			throw new IllegalArgumentException("Invalid input data");
		}

		final BufferedDataTable dataTable = inData[0];

		final Set<FeatureType> selFeatures = m_settings.getSelectedFeatures();
		final BufferedDataTable resultTable;
		if((selFeatures == null) || selFeatures.isEmpty()){
			setWarningMessage("No feature is selected. Node returns the original unaltered table.");
			resultTable = dataTable;
		}else{
			final ColumnRearranger rearranger = createColumnRearranger(
					dataTable.getDataTableSpec());
			resultTable = exec.createColumnRearrangeTable(dataTable, rearranger, exec);
		}

		return new BufferedDataTable[]{resultTable};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		if ((inSpecs == null) || (inSpecs.length < 1)) {
			throw new InvalidSettingsException("Invalid input spec");
		}

		final Set<FeatureType> selectedTypes = m_settings.getSelectedFeatures();
		if((selectedTypes == null) || selectedTypes.isEmpty()){
			setWarningMessage("No feature is selected.");
		}

		final DataTableSpec inSpec = inSpecs[0];
		m_audioColumnSelectionSettingsModel.configure(inSpec);

		checkAggregatorSelected(m_aggregatorSettingsModel);

		return new DataTableSpec[]{createColumnRearranger(inSpec).createSpec()};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_audioColumnSelectionSettingsModel.saveSettingsTo(settings);
		m_windowSizeSettingsModel.saveSettingsTo(settings);
		m_windowOverlapSettingsModel.saveSettingsTo(settings);
		for (final SettingsModelBoolean model : m_aggregatorSettingsModel) {
			model.saveSettingsTo(settings);
		}
		m_firstDerivativeModel.saveSettingsTo(settings);
		m_secondDerivativeModel.saveSettingsTo(settings);
		m_settings.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_audioColumnSelectionSettingsModel.loadSettingsFrom(settings);
		m_windowSizeSettingsModel.loadSettingsFrom(settings);
		m_windowOverlapSettingsModel.loadSettingsFrom(settings);
		for (final SettingsModelBoolean model : m_aggregatorSettingsModel) {
			model.loadSettingsFrom(settings);
		}
		m_firstDerivativeModel.loadSettingsFrom(settings);
		m_secondDerivativeModel.loadSettingsFrom(settings);
		m_settings.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_audioColumnSelectionSettingsModel.validateSettings(settings);
		m_windowSizeSettingsModel.validateSettings(settings);
		m_windowOverlapSettingsModel.validateSettings(settings);
		for (final SettingsModelBoolean model : m_aggregatorSettingsModel) {
			model.validateSettings(settings);
		}
		m_firstDerivativeModel.validateSettings(settings);
		m_secondDerivativeModel.validateSettings(settings);
		m_settings.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
	CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
	CanceledExecutionException {
		// TODO: generated method stub
	}

	private ColumnRearranger createColumnRearranger(final DataTableSpec inSpec) throws InvalidSettingsException {
		final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
		final int colIdx = m_audioColumnSelectionSettingsModel.getSelectedColumnIndex();

		// Create new DataColumnSpec for the extracted values
		final List<DataColumnSpec> colSpecsList = new ArrayList<DataColumnSpec>();
		final Set<FeatureType> selectedFeatures = m_settings.getSelectedFeatures();
		final FeatureExtractor[] featureExtractors = FeatureExtractor
				.getFeatureExtractors(selectedFeatures.toArray(
						new FeatureType[selectedFeatures.size()]));

		final List<FeatureExtractor.Aggregator> selectedAggregators = new ArrayList<FeatureExtractor.Aggregator>();
		for (final SettingsModelBoolean model : m_aggregatorSettingsModel) {
			if (model.getBooleanValue()) {
				selectedAggregators.add(FeatureExtractor.getAggregator(model.getConfigName()));
			}
		}

		if (selectedAggregators.isEmpty()) {
			throw new InvalidSettingsException("At least one aggregator must be selected.");
		}

		final Set<String> colNames = new HashSet<String>(
				Arrays.asList(inSpec.getColumnNames()));
		for(final FeatureExtractor extractor : featureExtractors){
			final FeatureType type = extractor.getType();
			if(type.hasParameters()){
				for(final String parameter : type.getParameters()){
					extractor.setParameterValue(parameter,
							m_settings.getParameterValue(type, parameter));
				}
			}

			final int dimension = extractor.getDimension(
					m_windowSizeSettingsModel.getIntValue());

			addColSpecToList(type.getName(), dimension, type.getDataType(), selectedAggregators, colNames,
					colSpecsList);

			if (m_firstDerivativeModel.getBooleanValue()) {
				addColSpecToList(type.getName() + "(1st Derivative)", dimension, type.getDataType(),
						selectedAggregators, colNames,
						colSpecsList);
			}

			if (m_secondDerivativeModel.getBooleanValue()) {
				addColSpecToList(type.getName() + "(2nd Derivative)", dimension, type.getDataType(),
						selectedAggregators, colNames, colSpecsList);
			}

			// for (final FeatureExtractor.Aggregator agg : selectedAggregators)
			// {
			// // Add the type name, so that the new name have suffix #1
			// final String aggSuffix = " - " + agg.getName();
			// if (dimension > 1) {
			// colNames.add(extractor.getType().getName() + aggSuffix);
			// }
			// for (int dim = 0; dim < dimension; dim++) {
			// final UniqueNameGenerator generator = new
			// UniqueNameGenerator(colNames);
			// final DataColumnSpec colSpec = generator.newColumn(type.getName()
			// + aggSuffix, type.getDataType());
			// colSpecsList.add(colSpec);
			// colNames.add(colSpec.getName());
			// }
			// }
		}

		final DataColumnSpec[] newColSpecs = colSpecsList.toArray(
				new DataColumnSpec[colSpecsList.size()]);

		final FeatureExtractorCellFactory cellFactory =
				new FeatureExtractorCellFactory(colIdx, newColSpecs,
						featureExtractors, m_windowSizeSettingsModel.getIntValue(),
						m_windowOverlapSettingsModel.getIntValue(),
						selectedAggregators.toArray(new FeatureExtractor.Aggregator[selectedAggregators.size()]),
						m_firstDerivativeModel.getBooleanValue(), m_secondDerivativeModel.getBooleanValue());

		rearranger.append(cellFactory);

		return rearranger;
	}

	private void addColSpecToList(final String colName, final int dimension, final DataType type,
			final List<FeatureExtractor.Aggregator> aggregators, final Set<String> colNames,
			final List<DataColumnSpec> colSpecsList) {
		for (final FeatureExtractor.Aggregator agg : aggregators) {
			// Add the type name, so that the new name have suffix #1
			final String newName = colName + " - " + agg.getName();
			if (dimension > 1) {
				colNames.add(newName);
			}
			for (int dim = 0; dim < dimension; dim++) {
				final UniqueNameGenerator generator = new UniqueNameGenerator(colNames);
				final DataColumnSpec colSpec = generator.newColumn(newName, type);
				colSpecsList.add(colSpec);
				colNames.add(colSpec.getName());
			}
		}
	}

	static void checkAggregatorSelected(final SettingsModelBoolean[] aggregatorsModel) throws InvalidSettingsException {
		boolean isSelected = false;
		for (final SettingsModelBoolean model : aggregatorsModel) {
			if (model.getBooleanValue()) {
				isSelected = true;
				break;
			}
		}
		if (!isSelected) {
			throw new InvalidSettingsException("At least one aggregator must be selected.");
		}
	}

}

