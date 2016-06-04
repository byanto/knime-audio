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
package org.knime.audio.data;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.knime.audio.data.io.BufferedDataInputStream;
import org.knime.audio.data.io.BufferedDataOutputStream;
import org.knime.audio.data.recognizer.RecognitionResult;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.util.UniqueNameGenerator;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
public class Audio {

    private File m_file;
    private Map<String, RecognitionResult> m_recognitionResults;

    /**
     * Prevent to directly create a new audio instance.
     * A new audio instance should only be created using {@link AudioBuilder}.
     */
    Audio(){}

    Audio(final File file) throws UnsupportedAudioFileException, IOException{
        if(file == null){
            throw new IllegalArgumentException("The input file cannot be null.");
        }
        if(file.isDirectory()){
            throw new IllegalArgumentException("File " + file.getName() + " is a directory.");
        }
        if(!file.exists()){
            throw new IllegalArgumentException("File " + file.getName() + " doesn't exist.");
        }
        m_file = file;
        m_recognitionResults = new LinkedHashMap<String, RecognitionResult>();
    }

    Audio(final File file, final Map<String, RecognitionResult> recognitionResults) {
        m_file = file;
        m_recognitionResults = new LinkedHashMap<String, RecognitionResult>();
        for (Entry<String, RecognitionResult> entry : recognitionResults.entrySet()) {
            m_recognitionResults.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return the filePath
     */
    public File getFile() {
        return m_file;
    }

    /**
     * @return the name of the audio file
     */
    public String getName(){
        return m_file.getName();
    }

    /**
     * @return the recognizers
     */
    public Map<String, RecognitionResult> getRecognitionResults() {
        return m_recognitionResults;
    }

    /**
     * @param key the key of the recognition result
     * @return the recognition result based on the given key
     */
    public RecognitionResult getRecognitionResult(final String key){
        return m_recognitionResults.get(key);
    }

    /**
     * Adds a recognition result to the list
     * @param result
     */
    public void addRecognitionResult(final RecognitionResult result){
        if(result == null){
            throw new IllegalArgumentException("result cannot be null");
        }

//        final String key = new UniqueNameGenerator(m_recognitionResults.keySet())
//                .newName(result.getRecognizerInfo(RecognizerInfo.KEY_NAME).toString());
        final String key = new UniqueNameGenerator(m_recognitionResults.keySet())
                .newName(result.getRecognizerName());
        m_recognitionResults.put(key, result);
    }

    /**
     * @return <code>true</code> if the audio has recognition result attached to it,
     * otherwise <code>false</code>
     */
    public boolean hasRecognitionResult(){
        return m_recognitionResults != null && !m_recognitionResults.isEmpty();
    }

    /**
     * @param output
     * @throws IOException
     */
    static void serialize( final Audio audio, final DataCellDataOutput output)
            throws IOException{
        output.writeUTF(audio.getFile().getAbsolutePath());
        if(audio.hasRecognitionResult()){
            output.writeBoolean(true);
            output.writeInt(audio.getRecognitionResults().size());
            for(Entry<String, RecognitionResult> entry : audio.getRecognitionResults().entrySet()){
                output.writeUTF(entry.getKey());
                RecognitionResult.serialize(output, entry.getValue());
            }
        }else{
            output.writeBoolean(false);
        }
    }

    /**
     *
     * @param input
     * @return an audio instance
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    static Audio deserialize(final DataCellDataInput input) throws
            IOException, UnsupportedAudioFileException {
        final Audio audio = new Audio(new File(input.readUTF()));
        if(input.readBoolean()){
            final int size = input.readInt();
            for(int i = 0; i < size; i++){
                final String key = input.readUTF();
                final RecognitionResult result = RecognitionResult.deserialize(input);
                audio.getRecognitionResults().put(key, result);
            }
        }

        return audio;
    }

    static void saveInternals(final Audio audio,
            final BufferedDataOutputStream output) throws IOException {

        final String path = audio.getFile().getAbsolutePath();
        output.writeInt(path.length());
        output.writeChars(path);
        if (audio.hasRecognitionResult()) {
            output.writeBoolean(true);
            output.writeInt(audio.getRecognitionResults().size());
            for(Entry<String, RecognitionResult> entry : audio.getRecognitionResults().entrySet()){
                output.writeInt(entry.getKey().length());
                output.writeChars(entry.getKey());
                RecognitionResult.saveInternals(output, entry.getValue());
            }
        } else {
            output.writeBoolean(false);
        }
    }

    static Audio loadInternals(final BufferedDataInputStream input)
            throws IOException, UnsupportedAudioFileException{
        final char[] path = new char[input.readInt()];
        input.read(path);
        final Audio audio = new Audio(new File(new String(path)));
        if(input.readBoolean()){
            final int size = input.readInt();
            for(int i = 0; i < size; i++){
                final char[] key = new char[input.readInt()];
                input.read(key);
                final RecognitionResult result = RecognitionResult.loadInternals(input);
                audio.getRecognitionResults().put(new String(key), result);
            }
        }
        return audio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_file == null) ? 0 : m_file.hashCode());
        result = prime * result + ((m_recognitionResults == null) ? 0 : m_recognitionResults.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Audio other = (Audio)obj;
        if (m_file == null) {
            if (other.m_file != null) {
                return false;
            }
        } else if (!m_file.equals(other.m_file)) {
            return false;
        }
        if (m_recognitionResults == null) {
            if (other.m_recognitionResults != null) {
                return false;
            }
        } else if (!m_recognitionResults.equals(other.m_recognitionResults)) {
            return false;
        }
        return true;
    }

}
