package org.knime.audio.node.recognizer.bing;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "BingSpeechRecognizer" Node.
 *
 *
 * @author Budi Yanto, KNIME.com
 */
public class BingSpeechSRNodeFactory
        extends NodeFactory<BingSpeechSRNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public BingSpeechSRNodeModel createNodeModel() {
        return new BingSpeechSRNodeModel();
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
    public NodeView<BingSpeechSRNodeModel> createNodeView(final int viewIndex,
            final BingSpeechSRNodeModel nodeModel) {
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
        return new BingSpeechSRNodeDialog();
    }

}

