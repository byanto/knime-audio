package org.knime.audio.node.mpeg7featureextractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.audio.data.feature.mpeg7.MPEG7FeatureType;
import org.knime.audio.dialogcomponent.AudioColumnSelection;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of MPEG7FeatureExtractor.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class MPEG7FeatureExtractorNodeModel extends NodeModel {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(MPEG7FeatureExtractorNodeModel.class);

	static final String MEAN = "Mean";
	static final String STD_DEVIATION = "Standard Deviation";

	private final AudioColumnSelection m_audioColumnSelectionModel = new AudioColumnSelection();
	private final SettingsModelString m_hopSizeModel = createHopSizeModel();
	private final SettingsModelString m_aggregatorModel = createAggregatorModel();
	private final MPEG7FeatureExtractorSettings m_settings = new MPEG7FeatureExtractorSettings();

	static SettingsModelString createHopSizeModel(){
		return new SettingsModelString("HopSize", "10");
	}

	static SettingsModelString createAggregatorModel(){
		return new SettingsModelString("Aggregator", MEAN);
	}

	/**
	 * Constructor for the node model.
	 */
	protected MPEG7FeatureExtractorNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		if ((inData == null) || (inData.length < 1)) {
			throw new IllegalArgumentException("Invalid input data");
		}

		final BufferedDataTable dataTable = inData[0];

		final Set<MPEG7FeatureType> selFeatures = m_settings.getSelectedFeatures();
		final BufferedDataTable resultTable;
		if ((selFeatures == null) || selFeatures.isEmpty()) {
			setWarningMessage("No feature is selected. Node returns the original unaltered table.");
			resultTable = dataTable;
		} else {
			final ColumnRearranger rearranger = createColumnRearranger(dataTable.getDataTableSpec());
			resultTable = exec.createColumnRearrangeTable(dataTable, rearranger, exec);
		}

		return new BufferedDataTable[] { resultTable };
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

		final Set<MPEG7FeatureType> selectedTypes = m_settings.getSelectedFeatures();
		if ((selectedTypes == null) || selectedTypes.isEmpty()) {
			setWarningMessage("No feature is selected.");
		}

		final DataTableSpec inSpec = inSpecs[0];
		m_audioColumnSelectionModel.configure(inSpec);

		return new DataTableSpec[] { createColumnRearranger(inSpec).createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_hopSizeModel.saveSettingsTo(settings);
		m_aggregatorModel.saveSettingsTo(settings);
		m_settings.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_hopSizeModel.loadSettingsFrom(settings);
		m_aggregatorModel.loadSettingsFrom(settings);
		m_settings.loadSettingsFrom(settings);
		m_settings.setHopSize(m_hopSizeModel.getStringValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_hopSizeModel.validateSettings(settings);
		m_aggregatorModel.validateSettings(settings);
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

	private ColumnRearranger createColumnRearranger(final DataTableSpec inSpec) {
		final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
		final int colIdx = m_audioColumnSelectionModel.getSelectedColumnIndex();
		final Set<MPEG7FeatureType> selectedFeatures = m_settings.getSelectedFeatures();
		final List<DataColumnSpec> colSpecList = new ArrayList<DataColumnSpec>();
		for (final MPEG7FeatureType type : selectedFeatures) {
			final DataColumnSpec[] specs = type.getColSpecs(inSpec);
			for (final DataColumnSpec cs : specs) {
				colSpecList.add(cs);
			}
		}

		rearranger.append(
				new MPEG7FeatureExtractorCellFactory(colIdx, m_aggregatorModel.getStringValue(),
						m_settings.getMpeg7Config(),
						selectedFeatures.toArray(new MPEG7FeatureType[selectedFeatures.size()]),
						colSpecList.toArray(new DataColumnSpec[colSpecList.size()])
						)
				);

		return rearranger;
	}

}

