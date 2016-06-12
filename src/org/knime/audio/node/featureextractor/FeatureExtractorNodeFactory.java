package org.knime.audio.node.featureextractor;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FeatureExtractor" Node.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class FeatureExtractorNodeFactory
extends NodeFactory<FeatureExtractorNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureExtractorNodeModel createNodeModel() {
		return new FeatureExtractorNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<FeatureExtractorNodeModel> createNodeView(final int viewIndex,
			final FeatureExtractorNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new FeatureExtractorNodeDialog();
	}

}

