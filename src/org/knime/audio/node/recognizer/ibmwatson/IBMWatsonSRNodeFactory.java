package org.knime.audio.node.recognizer.ibmwatson;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "IBMWatsonRecognizer" Node.
 * 
 *
 * @author Budi Yanto, KNIME.com
 */
public class IBMWatsonSRNodeFactory 
        extends NodeFactory<IBMWatsonSRNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public IBMWatsonSRNodeModel createNodeModel() {
        return new IBMWatsonSRNodeModel();
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
    public NodeView<IBMWatsonSRNodeModel> createNodeView(final int viewIndex,
            final IBMWatsonSRNodeModel nodeModel) {
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
        return new IBMWatsonSRNodeDialog();
    }

}

