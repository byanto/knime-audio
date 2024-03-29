package org.knime.audio.node.recognizer.google;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "GoogleSpeech" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Budi Yanto, KNIME.com
 */
public class GoogleSpeechNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the GoogleSpeech node.
     */
    protected GoogleSpeechNodeDialog() {

    }
}

