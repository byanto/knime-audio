package org.knime.audio.node.reader;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "AudioReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Budi Yanto, KNIME.com
 */
public class AudioReaderNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the AudioReader node.
     */
    protected AudioReaderNodeDialog() {

    }
}

