package org.knime.audio.node.recognizer.google;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GoogleSpeech" Node.
 * 
 *
 * @author Budi Yanto, KNIME.com
 */
public class GoogleSpeechNodeFactory 
        extends NodeFactory<GoogleSpeechNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public GoogleSpeechNodeModel createNodeModel() {
        return new GoogleSpeechNodeModel();
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
    public NodeView<GoogleSpeechNodeModel> createNodeView(final int viewIndex,
            final GoogleSpeechNodeModel nodeModel) {
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
        return new GoogleSpeechNodeDialog();
    }

}

