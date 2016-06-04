package org.knime.audio.node.recognizer.cmusphinx;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "CMUSphinxRecognizer" Node.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class CMUSphinxSRNodeFactory
        extends NodeFactory<CMUSphinxSRNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CMUSphinxSRNodeModel createNodeModel() {
        return new CMUSphinxSRNodeModel();
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
    public NodeView<CMUSphinxSRNodeModel> createNodeView(final int viewIndex,
            final CMUSphinxSRNodeModel nodeModel) {
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
        return new CMUSphinxSRNodeDialog();
    }

}

