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
 *   May 6, 2016 (budiyanto): created
 */
package org.knime.audio.node.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.knime.audio.data.Audio;
import org.knime.audio.data.recognizer.RecognitionResult;
import org.knime.audio.util.AudioErrorUtils;
import org.knime.audio.util.AudioUtils;
import org.knime.core.node.NodeLogger;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
class AudioCellView extends JPanel{

    /**
     * Automatically generated Serial Version UID
     */
    private static final long serialVersionUID = -4234704116274105268L;

    private static final NodeLogger LOGGER = NodeLogger.getLogger(AudioCellView.class);

    private Audio m_audio;

    private int m_totalSamples;

    AudioCellView(final Audio audio){
        m_audio = audio;
        setLayout(new BorderLayout());
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Viewer", createViewerPanel());
        tabbedPane.add("Recognition", createRecognitionPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JSplitPane createViewerPanel(){
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setPreferredSize(new Dimension(1000, 500));
        final JScrollPane waveScrollPane = new JScrollPane(createAudioWavePanel());
        waveScrollPane.setMinimumSize(new Dimension(700, 600));

        final JScrollPane infoScrollPane = new JScrollPane(createAudioInfoPanel());
        infoScrollPane.setMinimumSize(new Dimension(200, 300));

        splitPane.setLeftComponent(waveScrollPane);
        splitPane.setRightComponent(infoScrollPane);
        splitPane.setDividerLocation(0.8);
        return splitPane;
    }

    private JPanel createAudioWavePanel(){
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Audio Wave"));

        try {
            final double[][] samples = AudioUtils.getSamples(m_audio);
            m_totalSamples = samples[0].length;
            for(int channel = 0; channel < samples.length; channel++){
                final XYSeriesCollection dataset = new XYSeriesCollection();
                final XYSeries series = new XYSeries("Audio Wave");
                for(int i = 0; i < samples[channel].length; i++){
                    series.add(i, samples[channel][i]);
                }
                dataset.addSeries(series);
                JFreeChart chart = ChartFactory.createXYLineChart(
                    "Channel " + (channel + 1), "Sample", "Value", dataset);
                chart.removeLegend();
                final JPanel chartPanel = new ChartPanel(chart);
                panel.add(chartPanel);
            }
        } catch (UnsupportedAudioFileException | IOException ex) {
            panel.add(new JLabel("Error generating audio wave panel for: "
                    + m_audio.getName()));
            LOGGER.error(ex.getMessage());
        }

        return panel;
    }

    private JPanel createAudioInfoPanel(){
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 300));
        panel.setBorder(BorderFactory.createTitledBorder("Audio Information"));

        final DefaultTableModel model = new DefaultTableModel(0, 2){

            private static final long serialVersionUID = 1L;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        AudioFileFormat fileFormat = null;

        try {
            fileFormat = AudioSystem.getAudioFileFormat(m_audio.getFile());
        } catch (UnsupportedAudioFileException | IOException ex) {
            AudioErrorUtils.showError(this, ex.getMessage(), "Cannot open file.");
            return null;
        }

        final AudioFormat format = fileFormat.getFormat();
        model.addRow(new Object[]{"Name", m_audio.getName()});
        model.addRow(new Object[]{"Path", m_audio.getFile().getAbsolutePath()});
        model.addRow(new Object[]{"Length in Bytes", fileFormat.getByteLength()});
        model.addRow(new Object[]{"Length in Seconds",
            AudioUtils.convertSamplesToTime(m_totalSamples, format.getSampleRate())});
        model.addRow(new Object[]{"Length in Frames", fileFormat.getFrameLength()});
        model.addRow(new Object[]{"Type", fileFormat.getType()});

        // Audio Format
        model.addRow(new Object[]{"Encoding", format.getEncoding()});
        model.addRow(new Object[]{"Sample Rate in Hz", format.getSampleRate()});
        model.addRow(new Object[]{"Sample Size in Bits", format.getSampleSizeInBits()});
        model.addRow(new Object[]{"Channels", format.getChannels()});
        model.addRow(new Object[]{"Frame Size", format.getFrameSize()});
        model.addRow(new Object[]{"Frame Rate", format.getFrameRate()});
        model.addRow(new Object[]{"Big Endian", format.isBigEndian()});

        final JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setTableHeader(null);
        table.getColumnModel().getColumn(0).setMinWidth(120);
        table.getColumnModel().getColumn(0).setMaxWidth(120);

        panel.add(new JScrollPane(table));

        return panel;
    }

    private JPanel createRecognitionPanel(){
        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(600, 400));

        if(!m_audio.hasRecognitionResult()){
            final JLabel label = new JLabel("No audio recognition is available yet.");
            mainPanel.add(label, BorderLayout.CENTER);
        }else{
            final JPanel descriptionPanel = new JPanel(new GridLayout(1, 1));
            final JEditorPane descriptionEditor = new JEditorPane("text/html", "No recognizer is selected.");
            descriptionEditor.setEditable(false);
            final JScrollPane descriptionScrollPane = new JScrollPane(descriptionEditor);
            descriptionScrollPane.setBorder(BorderFactory.createTitledBorder("Information"));
            descriptionScrollPane.setMinimumSize(new Dimension(400, 200));
            descriptionPanel.add(descriptionScrollPane);

            final JPanel recognizersPanel = new JPanel(new GridLayout(1, 1));
            final Set<String> recognizerSet = m_audio.getRecognitionResults().keySet();
//            final Set<String> recognizerSet = m_audio.getRecognizers().keySet();
            final JList<String> recognizerList = new JList<String>(
                    recognizerSet.toArray(new String[recognizerSet.size()]));
            recognizerList.setSelectedIndex(0);
            updateRecognitionDescriptionEditor(descriptionEditor,
                recognizerList.getSelectedValue());
            recognizerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            recognizerList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    if(!e.getValueIsAdjusting()){
                        updateRecognitionDescriptionEditor(descriptionEditor,
                            recognizerList.getSelectedValue());
                    }
                }
            });
            final JScrollPane recognizerScrollPane = new JScrollPane(recognizerList);
            recognizerScrollPane.setMinimumSize(new Dimension(250, 200));
            recognizerScrollPane.setBorder(BorderFactory.createTitledBorder("Recognizers"));
            recognizersPanel.add(recognizerScrollPane);

            final JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, recognizersPanel, descriptionPanel);

            mainPanel.add(splitPane, BorderLayout.CENTER);

        }

        return mainPanel;
    }

    private void updateRecognitionDescriptionEditor(final JEditorPane editor,
            final String recognizerKey){
        final RecognitionResult result = m_audio.getRecognitionResult(recognizerKey);
        final StringBuilder builder = new StringBuilder()
        .append("<h2>").append(recognizerKey).append("</h2>")
        .append("<h3>Recognizer Info</h3>")
//        .append("Type: ").append(result.getRecognizerInfo(RecognizerInfo.KEY_NAME))
        .append("Type: ").append(result.getRecognizerName())
        .append("<h3>Recognition Result</h3>")
        .append("Transcript: ").append(result.getTranscript()).append("<br/>")
        .append("Confidence: ");
        if(result.getConfidence() == RecognitionResult.UNKNOWN_CONFIDENCE_SCORE){
            builder.append("Unknown");
        }else{
            builder.append(result.getConfidence());
        }
        editor.setText(builder.toString());
    }

}
