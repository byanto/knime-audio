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
 *   Jun 6, 2016 (budiyanto): created
 */
package org.knime.audio.data.feature.mpeg7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.sound.sampled.AudioInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.crysandt.audio.AudioInFloat;
import de.crysandt.audio.AudioInFloatSampled;
import de.crysandt.audio.mpeg7audio.Config;
import de.crysandt.audio.mpeg7audio.Encoder;
import de.crysandt.audio.mpeg7audio.mci.CreationInformation;
import de.crysandt.audio.mpeg7audio.mci.MediaInformation;
import de.crysandt.audio.mpeg7audio.msgs.Msg;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioFundamentalFrequency;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioHarmonicity;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioPower;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioSignature;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioSpectrumBasisProjection;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioSpectrumCentroid;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioSpectrumDistribution;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioSpectrumEnvelope;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioSpectrumFlatness;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioSpectrumSpread;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioTempoType;
import de.crysandt.audio.mpeg7audio.msgs.MsgAudioWaveform;
import de.crysandt.audio.mpeg7audio.msgs.MsgBackgroundNoiseLevel;
import de.crysandt.audio.mpeg7audio.msgs.MsgBandWidth;
import de.crysandt.audio.mpeg7audio.msgs.MsgClick;
import de.crysandt.audio.mpeg7audio.msgs.MsgDcOffset;
import de.crysandt.audio.mpeg7audio.msgs.MsgDigitalClip;
import de.crysandt.audio.mpeg7audio.msgs.MsgDigitalZero;
import de.crysandt.audio.mpeg7audio.msgs.MsgHarmonicSpectralCentroid;
import de.crysandt.audio.mpeg7audio.msgs.MsgHarmonicSpectralDeviation;
import de.crysandt.audio.mpeg7audio.msgs.MsgHarmonicSpectralSpread;
import de.crysandt.audio.mpeg7audio.msgs.MsgHarmonicSpectralVariation;
import de.crysandt.audio.mpeg7audio.msgs.MsgListener;
import de.crysandt.audio.mpeg7audio.msgs.MsgLogAttackTime;
import de.crysandt.audio.mpeg7audio.msgs.MsgResizer;
import de.crysandt.audio.mpeg7audio.msgs.MsgSampleHold;
import de.crysandt.audio.mpeg7audio.msgs.MsgSilence;
import de.crysandt.audio.mpeg7audio.msgs.MsgSoundModel;
import de.crysandt.audio.mpeg7audio.msgs.MsgSpectralCentroid;
import de.crysandt.audio.mpeg7audio.msgs.MsgTemporalCentroid;
import de.crysandt.xml.Namespace;

/**
 *
 * @author Budi Yanto, Berlin, KNIME.com
 */
public class MPEG7DocumentBuilder implements MsgListener {

	public static int CENTROID_IDX = 0;
	public static int SPREAD_IDX = 1;

	public static int MIN_IDX = 0;
	public static int MAX_IDX = 1;

	public static int HARMONIC_RATIO_IDX = 0;
	public static int UPPER_LIMIT_IDX = 1;

	private static final String HOP_SIZE = "hopSize";
	private static final String NR_OF_SAMPLES = "totalNumOfSamples";
	private static final String XSI_TYPE = "xsi:type";
	private static final String VECTOR_SIZE = "vectorSize";

	private final Map<String, MPEG7AudioDescriptor> m_descriptors = new HashMap<String, MPEG7AudioDescriptor>();
	private Document m_doc = null;
	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String SPACE = " ";

	private int duration = 0;
	private final TreeMap<String, String> schema_location = new TreeMap<String, String>();

	// start with ArrayList (optimal for add()); sort messages later
	private final List<Msg> listAW = new ArrayList<Msg>(); // Audio Waveform
	private final List<Msg> listAP = new ArrayList<Msg>(); // Audio Power
	private final List<Msg> listASE = new ArrayList<Msg>(); // Audio Spectrum Envelope															// Envelope
	private final List<Msg> listASC = new ArrayList<Msg>(); // Audio Spectrum Centroid
	private final List<Msg> listASS = new ArrayList<Msg>(); // Audio Spectrum Spread
	private final List<Msg> listASF = new ArrayList<Msg>(); // Audio Spectrum Flatness
	private final List<Msg> listAH = new ArrayList<Msg>(); // Audio Harmonicity
	private final List<Msg> listAFF = new ArrayList<Msg>(); // Audio Fundamental Frequency
	private MsgLogAttackTime msgLAT = null; // Log Attack Time
	private MsgTemporalCentroid msgTC  = null; // Temporal Centroid
	private MsgHarmonicSpectralCentroid  msgHSC = null; // Harmonic Spectral Centroid
	private MsgHarmonicSpectralDeviation msgHSD = null; // Harmonic Spectral Deviation
	private MsgHarmonicSpectralSpread    msgHSS = null; // Harmonic Spectral Spread
	private MsgHarmonicSpectralVariation msgHSV = null; // Harmonic Spectral Variation
	private MsgSpectralCentroid msgSC  = null; // SpectralCentroid
	private final List<Msg> listASBP = new ArrayList<Msg>(); // Audio Spectrum Basis Projection
	private final List<Msg> listASD = new ArrayList<Msg>();
	private final List<Msg> listDC = new ArrayList<Msg>();// DigitalClip
	private final List<Msg> listDZ = new ArrayList<Msg>();// DigitalZero
	private final List<Msg> listSH = new ArrayList<Msg>();// SampleHold
	private final List<Msg> listCK = new ArrayList<Msg>();// Click
	private final List<Msg> listBNL = new ArrayList<Msg>();// BackgroundNoiseLevel
	private final List<Msg> listDCO = new ArrayList<Msg>();// DcOffset
	private final List<Msg> listBW = new ArrayList<Msg>();// BandWidth
	private final List<Msg> listAS = new ArrayList<Msg>();
	private final List<Msg> listSI = new ArrayList<Msg>();
	private final List<Msg> listBPM = new ArrayList<Msg>();
	private MsgSoundModel msg_sound_model = null;
	private MediaInformation media_information = null;
	private CreationInformation creation_information = null;

	public void setMediaInformation(final MediaInformation mi) {
		media_information = mi;
	}

	public void setCreationInformation(final CreationInformation ci) {
		creation_information = ci;
	}

	public void addSchemaLocation(final String schema, final String location) {
		schema_location.put(schema, location);
	}

	private void addMediaInformation(final Document doc, final Element audio_segment) {
		if (media_information == null) {
			return;
		}

		audio_segment.appendChild(media_information.toXML(doc, "MediaInformation"));
	}

	private void addCreationInformation(final Document doc, final Element audio_segment) {
		if (creation_information == null) {
			return;
		}

		audio_segment.appendChild(creation_information.toXML(doc, "CreationInformation"));
	}

	private void setDuration(final MsgResizer msg) {
		duration = Math.max(duration, msg.time + msg.duration);
	}

	public static Element getSeriesOfScalar(final Document doc, final int hop_size, final int num_of_samples) {
		final Element sos = doc.createElementNS(Namespace.MPEG7, "SeriesOfScalar");

		sos.setAttribute("hopSize", getMediaDuration(hop_size));
		sos.setAttribute("totalNumOfSamples", "" + num_of_samples);
		return sos;
	}

	public static Element getSeriesOfVector(final Document doc, final int hop_size, final int rows, final int cols) {
		final Element sov = doc.createElementNS(Namespace.MPEG7, "SeriesOfVector");

		sov.setAttribute("hopSize", getMediaDuration(hop_size));
		sov.setAttribute("totalNumOfSamples", "" + (rows * cols));
		sov.setAttribute("vectorSize", "" + cols);

		return sov;
	}

	/**
	 * Creates a MediaDuration from a duration given in milliseconds
	 *
	 * @param time
	 *            int time in milliseconds
	 * @return String MPEG-7 compliant MediaDuration
	 */
	private static String getMediaDuration(int time) {
		assert time > 0;

		final int msec = time % 1000;
		final int sec = (time /= 1000) % 60;
		final int min = (time /= 60) % 60;

		final StringBuffer duration = new StringBuffer("PT");

		if (min > 0) {
			duration.append(min);
			duration.append("M");
		}

		if ((sec > 0) || (min > 0)) {
			duration.append(sec);
			duration.append("S");
		}

		if (msec > 0) {
			duration.append(msec);
			duration.append("N1000F");
		}

		return duration.toString();
	}

	private static String getOctaveResolution(final float resolution) {
		if (resolution >= 1) {
			return "" + Math.round(resolution);
		}

		return "1/" + Math.round(1.0f / resolution);
	}

	private static StringBuffer append(final StringBuffer buffer, final float[] vector) {
		if ((vector != null) && (vector.length > 0)) {
			buffer.append(vector[0]);
			for (int i = 1; i < vector.length; ++i) {
				buffer.append(SPACE).append(vector[i]);
			}
		}

		return buffer;
	}

	private static StringBuffer append(final StringBuffer buffer, final float[][] matrix) {
		if ((matrix != null) && (matrix.length > 0)) {
			for (int i = 0; i < matrix.length; ++i) {
				append(buffer, matrix[i]);
				buffer.append(NEWLINE);
			}
		}

		return buffer;
	}

	/**
	 * @param x
	 *            float value to be converted
	 * @return Returns String of float value
	 */
	private static String format(final float x) {
		return "" + x;
	}

	protected Element createFrame(final Document doc) {
		final Element mpeg7 = doc.createElementNS(Namespace.MPEG7, "Mpeg7");

		// add some namespaces
		mpeg7.setAttributeNS(Namespace.XMLNS, "xmlns", Namespace.MPEG7);
		mpeg7.setAttributeNS(Namespace.XMLNS, "xmlns:mpeg7", Namespace.MPEG7);
		mpeg7.setAttributeNS(Namespace.XMLNS, "xmlns:xsi", Namespace.XSI);
		doc.appendChild(mpeg7);

		final Element description = doc.createElementNS(Namespace.MPEG7, "Description");
		description.setAttributeNS(Namespace.XSI, "xsi:type", "ContentEntityType");
		mpeg7.appendChild(description);

		final Element mmContent = doc.createElementNS(Namespace.MPEG7, "MultimediaContent");
		mmContent.setAttributeNS(Namespace.XSI, "xsi:type", "AudioType");
		description.appendChild(mmContent);

		final Element audioSegment = doc.createElementNS(Namespace.MPEG7, "Audio");
		audioSegment.setAttributeNS(Namespace.XSI, "xsi:type", "AudioSegmentType");
		mmContent.appendChild(audioSegment);

		return audioSegment;
	}

	/**
	 * @return the MPEG-7 document
	 */
	public Document getDocument() {
		return m_doc;
	}

	/**
	 * @return the MPEG-7 audio low-level descriptors
	 */
	public Map<String, MPEG7AudioDescriptor> getDescriptors() {
		return m_descriptors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedMsg(final Msg msg) {
		if (msg instanceof MsgResizer) {
			setDuration((MsgResizer) msg);
		} else if (msg instanceof MsgDigitalClip) {
			listDC.add(msg);
		} else if (msg instanceof MsgDigitalZero) {
			listDZ.add(msg);
		} else if (msg instanceof MsgSampleHold) {
			listSH.add(msg);
		} else if (msg instanceof MsgClick) {
			listCK.add(msg);
		} else if (msg instanceof MsgBackgroundNoiseLevel) {
			listBNL.add(msg);
		} else if (msg instanceof MsgDcOffset) {
			listDCO.add(msg);
		} else if (msg instanceof MsgBandWidth) {
			listBW.add(msg);
		} else if (msg instanceof MsgAudioSignature) {
			listAS.add(msg);
		} else if (msg instanceof MsgAudioSpectrumBasisProjection) {
			listASBP.add(msg);
		} else if (msg instanceof MsgAudioSpectrumCentroid) {
			listASC.add(msg);
		} else if (msg instanceof MsgAudioSpectrumSpread) {
			listASS.add(msg);
		} else if (msg instanceof MsgAudioPower) {
			listAP.add(msg);
		} else if (msg instanceof MsgAudioSpectrumDistribution) {
			listASD.add(msg);
		} else if (msg instanceof MsgAudioSpectrumEnvelope) {
			listASE.add(msg);
		} else if (msg instanceof MsgAudioSpectrumFlatness) {
			listASF.add(msg);
		} else if (msg instanceof MsgAudioWaveform) {
			listAW.add(msg);
		} else if (msg instanceof MsgAudioFundamentalFrequency) {
			listAFF.add(msg);
		} else if (msg instanceof MsgAudioHarmonicity) {
			listAH.add(msg);
		} else if (msg instanceof MsgSilence) {
			listSI.add(msg);
		} else if (msg instanceof MsgAudioTempoType) {
			listBPM.add(msg);
		} else if (msg instanceof MsgHarmonicSpectralCentroid) {
			assert msgHSC == null;
			msgHSC = (MsgHarmonicSpectralCentroid) msg;
		} else if (msg instanceof MsgHarmonicSpectralDeviation) {
			assert msgHSD == null;
			msgHSD = (MsgHarmonicSpectralDeviation) msg;
		} else if (msg instanceof MsgHarmonicSpectralSpread) {
			assert msgHSS == null;
			msgHSS = (MsgHarmonicSpectralSpread) msg;
		} else if (msg instanceof MsgHarmonicSpectralVariation) {
			assert msgHSV == null;
			msgHSV = (MsgHarmonicSpectralVariation) msg;
		} else if (msg instanceof MsgSoundModel) {
			assert msg_sound_model == null;
			msg_sound_model = (MsgSoundModel) msg;
		} else if (msg instanceof MsgLogAttackTime) { // RP 29/04/09 activate
			// LogAttackTime
			assert msgLAT == null;
			msgLAT = (MsgLogAttackTime) msg;
		} else if (msg instanceof MsgSpectralCentroid) { // RP 29/04/09 activate
			// SpectralCentroid
			assert msgSC == null;
			msgSC = (MsgSpectralCentroid) msg;
		} else if (msg instanceof MsgTemporalCentroid) { // RP 29/04/09 activate
			// TemporalCentroid
			assert msgTC == null;
			msgTC = (MsgTemporalCentroid) msg;
		}

	}

	public void encode(final AudioInputStream ais, final Config config, final boolean createDoc)
			throws ParserConfigurationException {
		final AudioInFloatSampled audioin = new AudioInFloatSampled(ais);
		final Encoder encoder = new Encoder(audioin.getSampleRate(), this, config);

		// copy audio signal from source to encoder
		float[] audio;
		while ((audio = audioin.get()) != null) {
			if (!audioin.isMono()) {
				audio = AudioInFloat.getMono(audio);
			}
			encoder.put(audio);
		}
		encoder.flush();
		build(createDoc);
	}

	private void build(final boolean createDoc) throws ParserConfigurationException {
		Element audioSegment = null;
		if (createDoc) {
			m_doc = createEmptyDocument();
			audioSegment = createFrame(m_doc);

			// add everything except descriptors and descrition schemes
			addMediaInformation(m_doc, audioSegment);
			addCreationInformation(m_doc, audioSegment);
		}

		encodeAP(m_doc, audioSegment);
		encodeAW(m_doc, audioSegment);
		encodeASE(m_doc, audioSegment);
		encodeASC(m_doc, audioSegment);
		encodeASS(m_doc, audioSegment);
		encodeASF(m_doc, audioSegment);
		encodeAH(m_doc, audioSegment);
		encodeAFF(m_doc, audioSegment);
		encodeLAT(m_doc, audioSegment);
		encodeTC(m_doc, audioSegment);
		encodeHSC(m_doc, audioSegment);
		encodeHSD(m_doc, audioSegment);
		encodeHSS(m_doc, audioSegment);
		encodeHSV(m_doc, audioSegment);
		encodeSC(m_doc, audioSegment);
		encodeASB(m_doc, audioSegment);
		encodeASP(m_doc, audioSegment);

		if (m_doc != null) {
			if (!schema_location.isEmpty()) {
				final StringBuffer buffer = new StringBuffer();
				for (final Entry<String, String> entry : schema_location.entrySet()) {
					buffer.append(entry.getKey()).append(SPACE);
					buffer.append(entry.getValue()).append(SPACE);
				}

				((Element) m_doc.getFirstChild()).setAttributeNS(Namespace.XSI, "xsi:schemaLocation",
						buffer.toString());
			}
			m_doc.normalize();
		}
		System.out.println("Descriptors size build: " + m_descriptors.size());
	}

	private static Document createEmptyDocument() throws ParserConfigurationException {
		final DocumentBuilderFactory doc_factory = DocumentBuilderFactory.newInstance();
		doc_factory.setNamespaceAware(true);
		final DocumentBuilder doc_builder = doc_factory.newDocumentBuilder();
		return doc_builder.newDocument();
	}

	@SuppressWarnings("unchecked")
	private void encodeAW(final Document doc, final Element audioSegment) {
		if (listAW.isEmpty()) {
			return;
		}

		Collections.sort(listAW);
		final MsgAudioWaveform msg = (MsgAudioWaveform) listAW.get(0);
		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(HOP_SIZE, msg.hopsize);
		prop.put(NR_OF_SAMPLES, listAW.size());

		final StringBuffer buffer_min = new StringBuffer();
		final StringBuffer buffer_max = new StringBuffer();
		final double[][] raw = new double[listAW.size()][2];
		for (int i = 0; i < listAW.size(); i++) {
			final MsgAudioWaveform m = (MsgAudioWaveform) listAW.get(i);
			raw[i][MIN_IDX] = m.min;
			raw[i][MAX_IDX] = m.max;
			if (doc != null) {
				buffer_min.append(format(msg.min));
				buffer_min.append(SPACE);

				buffer_max.append(format(msg.max));
				buffer_max.append(SPACE);
			}
		}

		final MPEG7AudioDescriptor descriptor = new MPEG7AudioDescriptor(raw, prop);
		m_descriptors.put(MPEG7FeatureType.AUDIO_WAVEFORM.getConfigName(), descriptor);

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioWaveformType");
			audioSegment.appendChild(audio_descriptor);

			final Element sos = getSeriesOfScalar(doc, msg.hopsize, listAW.size());
			audio_descriptor.appendChild(sos);

			final Element min = doc.createElementNS(Namespace.MPEG7, "Min");
			min.appendChild(doc.createTextNode(buffer_min.toString().trim()));
			sos.appendChild(min);

			final Element max = doc.createElementNS(Namespace.MPEG7, "Max");
			max.appendChild(doc.createTextNode(buffer_max.toString().trim()));
			sos.appendChild(max);
		}
	}

	@SuppressWarnings("unchecked")
	private void encodeAP(final Document doc, final Element audioSegment) {
		if (listAP.isEmpty()) {
			return;
		}

		Collections.sort(listAP);
		final MsgAudioPower msg = (MsgAudioPower) listAP.get(0);
		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(HOP_SIZE, msg.hopsize);
		prop.put(NR_OF_SAMPLES, listAP.size());

		final StringBuffer buffer = new StringBuffer();
		final double[][] rawData = new double[listAP.size()][1];
		for (int i = 0; i < listAP.size(); i++) {
			final MsgAudioPower m = (MsgAudioPower) listAP.get(i);
			rawData[i][0] = m.power;
			if (doc != null) {
				buffer.append(format(m.power));
				buffer.append(SPACE);
			}
		}

		final MPEG7AudioDescriptor descriptor = new MPEG7AudioDescriptor(rawData, prop);
		m_descriptors.put(MPEG7FeatureType.AUDIO_POWER.getConfigName(), descriptor);

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioPowerType");
			if (msg.db_scale) {
				audio_descriptor.setAttribute("dbScale", "true");
				schema_location.put(Namespace.MPEG7,
						"http://www.ient.rwth-aachen.de" + "/team/crysandt/mpeg7mds/mpeg7patched.xml");
			}
			audioSegment.appendChild(audio_descriptor);

			final Element sos = getSeriesOfScalar(doc, msg.hopsize, listAP.size());
			audio_descriptor.appendChild(sos);

			final Element raw = doc.createElementNS(Namespace.MPEG7, "Raw");
			sos.appendChild(raw);
			raw.appendChild(doc.createTextNode(buffer.toString().trim()));
		}
	}

	@SuppressWarnings("unchecked")
	private void encodeASE(final Document doc, final Element audioSegment) {
		if (listASE.isEmpty()) {
			return;
		}

		Collections.sort(listASE);
		final MsgAudioSpectrumEnvelope msg = (MsgAudioSpectrumEnvelope) listASE.get(0);

		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put("loEdge", msg.lo_edge);
		prop.put("hiEdge", msg.hi_edge);
		prop.put("octaveResolution", msg.resolution);
		prop.put("dbScale", msg.db_scale);
		String normalize = "off";
		switch (msg.normalize) {
		case 0:
			normalize = "off";
			break;
		case 1:
			normalize = "norm2";
			break;
		case 2:
			normalize = "power";
			break;
		default:
			assert false;
		}
		prop.put("normalize", normalize);
		final int rows = listASE.size();
		final int cols = msg.getEnvelopeLength();
		prop.put(NR_OF_SAMPLES, rows * cols);
		prop.put(VECTOR_SIZE, cols);

		final StringBuffer buffer = new StringBuffer();
		final double[][] rawData = new double[rows][cols];
		for (int i = 0; i < listASE.size(); i++) {
			final MsgAudioSpectrumEnvelope m = (MsgAudioSpectrumEnvelope) listASE.get(i);
			final float[] envelope = m.getEnvelope();
			assert envelope.length == cols;
			for (int c = 0; c < cols; ++c) {
				rawData[i][c] = envelope[c];
				if (doc != null) {
					buffer.append(envelope[c]);
					buffer.append(SPACE);
				}
			}
			if (doc != null) {
				buffer.append(NEWLINE);
			}
		}

		final MPEG7AudioDescriptor descriptor = new MPEG7AudioDescriptor(rawData, prop);
		m_descriptors.put(MPEG7FeatureType.AUDIO_SPECTRUM_ENVELOPE.getConfigName(), descriptor);

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");

			audio_descriptor.setAttribute("loEdge", "" + msg.lo_edge);
			audio_descriptor.setAttribute("hiEdge", "" + msg.hi_edge);
			audio_descriptor.setAttribute("octaveResolution", getOctaveResolution(msg.resolution));
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioSpectrumEnvelopeType");

			if (msg.db_scale) {
				audio_descriptor.setAttribute("dbScale", "true");
			}

			if (!normalize.equals("off")) {
				audio_descriptor.setAttribute("normalize", normalize);
			}

			if (msg.db_scale || (!normalize.equals("off"))) {
				schema_location.put(Namespace.MPEG7,
						"http://www.ient.rwth-aachen.de" + "/team/crysandt/mpeg7mds/mpeg7patched.xml");
			}

			audioSegment.appendChild(audio_descriptor);

			final Element sov = getSeriesOfVector(doc, msg.hopsize, rows, cols);
			audio_descriptor.appendChild(sov);

			final Element raw = doc.createElementNS(Namespace.MPEG7, "Raw");
			raw.setAttributeNS(Namespace.MPEG7, "mpeg7:dim", rows + " " + cols);
			sov.appendChild(raw);
			raw.appendChild(doc.createTextNode(buffer.toString()));
		}

	}

	@SuppressWarnings("unchecked")
	private void encodeASC(final Document doc, final Element audioSegment) {
		if (listASC.isEmpty()) {
			return;
		}

		Collections.sort(listASC);
		final MsgAudioSpectrumCentroid msg = (MsgAudioSpectrumCentroid) listASC.get(0);

		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(HOP_SIZE, msg.hopsize);
		prop.put(NR_OF_SAMPLES, listASC.size());

		MPEG7AudioDescriptor descriptor = m_descriptors.get(MPEG7FeatureType.AUDIO_SPECTRUM_CENTROID_SPREAD);
		double[][] rawData = null;
		if (descriptor == null) {
			rawData = new double[listASC.size()][2];
			descriptor = new MPEG7AudioDescriptor(rawData, prop);
			m_descriptors.put(MPEG7FeatureType.AUDIO_SPECTRUM_CENTROID_SPREAD.getConfigName(), descriptor);
		} else {
			rawData = descriptor.getRaw();
		}

		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < listASC.size(); i++) {
			final MsgAudioSpectrumCentroid m = (MsgAudioSpectrumCentroid) listASC.get(i);
			rawData[i][CENTROID_IDX] = m.centroid;

			if (doc != null) {
				buffer.append("" + format(m.centroid));
				buffer.append(SPACE);
			}
		}

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioSpectrumCentroidType");
			audioSegment.appendChild(audio_descriptor);

			final Element sos = getSeriesOfScalar(doc, msg.hopsize, listASC.size());
			audio_descriptor.appendChild(sos);

			final Element raw = doc.createElementNS(Namespace.MPEG7, "Raw");
			sos.appendChild(raw);
			raw.appendChild(doc.createTextNode(buffer.toString()));
		}
	}

	@SuppressWarnings("unchecked")
	private void encodeASS(final Document doc, final Element audioSegment) {
		if (listASS.isEmpty()) {
			return;
		}

		Collections.sort(listASS);
		final MsgAudioSpectrumSpread msg = (MsgAudioSpectrumSpread) listASS.get(0);

		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(HOP_SIZE, msg.hopsize);
		prop.put(NR_OF_SAMPLES, listASS.size());

		MPEG7AudioDescriptor descriptor = m_descriptors.get(MPEG7FeatureType.AUDIO_SPECTRUM_CENTROID_SPREAD);
		double[][] rawData = null;
		if (descriptor == null) {
			rawData = new double[listASS.size()][2];
			descriptor = new MPEG7AudioDescriptor(rawData, prop);
			m_descriptors.put(MPEG7FeatureType.AUDIO_SPECTRUM_CENTROID_SPREAD.getConfigName(), descriptor);
		} else {
			rawData = descriptor.getRaw();
		}

		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < listASS.size(); i++) {
			final MsgAudioSpectrumSpread m = (MsgAudioSpectrumSpread) listASS.get(i);
			rawData[i][SPREAD_IDX] = m.spread;

			if (doc != null) {
				buffer.append("" + format(m.spread));
				buffer.append(SPACE);
			}
		}

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioSpectrumSpreadType");
			audioSegment.appendChild(audio_descriptor);

			final Element sos = getSeriesOfScalar(doc, msg.hopsize, listASS.size());
			audio_descriptor.appendChild(sos);

			final Element raw = doc.createElementNS(Namespace.MPEG7, "Raw");
			sos.appendChild(raw);
			raw.appendChild(doc.createTextNode(buffer.toString()));
		}

	}

	@SuppressWarnings("unchecked")
	private void encodeASF(final Document doc, final Element audioSegment) {
		if (listASF.isEmpty()) {
			return;
		}

		Collections.sort(listASF);
		final MsgAudioSpectrumFlatness msg = (MsgAudioSpectrumFlatness) listASF.get(0);

		final int rows = listASF.size();
		final int cols = msg.getFlatnessLength();

		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put("loEdge", msg.lo_edge);
		prop.put("hiEdge", msg.hi_edge);
		prop.put(NR_OF_SAMPLES, rows * cols);
		prop.put(VECTOR_SIZE, cols);

		final StringBuffer buffer = new StringBuffer();
		final double[][] rawData = new double[rows][cols];
		for (int i = 0; i < listASF.size(); i++) {
			final float[] flatness = ((MsgAudioSpectrumFlatness) listASF.get(i)).getFlatness();
			assert flatness.length == cols;
			for (int c = 0; c < cols; ++c) {
				rawData[i][c] = flatness[c];
				if (doc != null) {
					buffer.append(flatness[c]);
					buffer.append(SPACE);
				}
			}
			if (doc != null) {
				buffer.append(NEWLINE);
			}
		}

		final MPEG7AudioDescriptor descriptor = new MPEG7AudioDescriptor(rawData, prop);
		m_descriptors.put(MPEG7FeatureType.AUDIO_SPECTRUM_FLATNESS.getConfigName(), descriptor);

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttribute("loEdge", "" + msg.lo_edge);
			audio_descriptor.setAttribute("hiEdge", "" + msg.hi_edge);
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioSpectrumFlatnessType");
			audioSegment.appendChild(audio_descriptor);

			final Element sov = getSeriesOfVector(doc, msg.hopsize, rows, cols);
			audio_descriptor.appendChild(sov);

			final Element raw = doc.createElementNS(Namespace.MPEG7, "Raw");
			raw.setAttributeNS(Namespace.MPEG7, "mpeg7:dim", rows + " " + cols);
			sov.appendChild(raw);
			raw.appendChild(doc.createTextNode(buffer.toString()));
		}
	}

	@SuppressWarnings("unchecked")
	private void encodeAH(final Document doc, final Element audioSegment) {
		if (listAH.isEmpty()) {
			return;
		}

		Collections.sort(listAH);
		final MsgAudioHarmonicity msg = (MsgAudioHarmonicity) listAH.get(0);

		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(HOP_SIZE, msg.hopsize);
		prop.put(NR_OF_SAMPLES, listAH.size());

		final StringBuffer buffer_hr = new StringBuffer();
		final StringBuffer buffer_ul = new StringBuffer();
		final double[][] raw = new double[listAH.size()][2];
		for (int i = 0; i < listAH.size(); i++) {
			final MsgAudioHarmonicity m = (MsgAudioHarmonicity) listAH.get(i);
			raw[i][HARMONIC_RATIO_IDX] = m.harmonicratio;
			raw[i][UPPER_LIMIT_IDX] = m.upperlimit;
			if (doc != null) {
				buffer_hr.append(msg.harmonicratio).append(SPACE);
				buffer_ul.append(msg.upperlimit).append(SPACE);
			}
		}

		final MPEG7AudioDescriptor descriptor = new MPEG7AudioDescriptor(raw, prop);
		m_descriptors.put(MPEG7FeatureType.AUDIO_HARMONICITY.getConfigName(), descriptor);

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioHarmonicityType");
			audioSegment.appendChild(audio_descriptor);

			final Element hr = doc.createElementNS(Namespace.MPEG7, "HarmonicRatio");
			audio_descriptor.appendChild(hr);

			final Element ul = doc.createElementNS(Namespace.MPEG7, "UpperLimitOfHarmonicity");
			audio_descriptor.appendChild(ul);

			final Element sos_hr = getSeriesOfScalar(doc, msg.hopsize, listAH.size());
			hr.appendChild(sos_hr);
			final Element raw_hr = doc.createElementNS(Namespace.MPEG7, "Raw");
			sos_hr.appendChild(raw_hr);

			final Element sos_ul = getSeriesOfScalar(doc, msg.hopsize, listAH.size());
			ul.appendChild(sos_ul);
			final Element raw_ul = doc.createElementNS(Namespace.MPEG7, "Raw");
			sos_ul.appendChild(raw_ul);

			raw_hr.appendChild(doc.createTextNode(buffer_hr.toString()));
			raw_ul.appendChild(doc.createTextNode(buffer_ul.toString()));
		}
	}

	@SuppressWarnings("unchecked")
	private void encodeAFF(final Document doc, final Element audioSegment) {
		if (this.listAFF.isEmpty()) {
			return;
		}

		Collections.sort(listAFF);
		final MsgAudioFundamentalFrequency msg = (MsgAudioFundamentalFrequency) listAFF.get(0);

		final Map<String, Object> prop = new HashMap<String, Object>();
		prop.put(HOP_SIZE, msg.hopsize);
		prop.put(NR_OF_SAMPLES, listAFF.size());
		prop.put("loLimit", msg.lolimit);
		prop.put("hiLimit", msg.hilimit);

		final StringBuffer buffer_aff = new StringBuffer();
		final double[][] rawData = new double[listAFF.size()][1];
		for (int i = 0; i < listAFF.size(); i++) {
			final MsgAudioFundamentalFrequency m = (MsgAudioFundamentalFrequency) listAFF.get(i);
			rawData[i][0] = m.fundfreq;
			if (doc != null) {
				buffer_aff.append(msg.fundfreq).append(SPACE);
			}
		}

		final MPEG7AudioDescriptor descriptor = new MPEG7AudioDescriptor(rawData, prop);
		m_descriptors.put(MPEG7FeatureType.AUDIO_FUNDAMENTAL_FREQUENCY.getConfigName(), descriptor);

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", "AudioFundamentalFrequencyType");
			audio_descriptor.setAttribute("loLimit", "" + msg.lolimit);
			audio_descriptor.setAttribute("hiLimit", "" + msg.hilimit);
			audioSegment.appendChild(audio_descriptor);

			final Element sos_aff = getSeriesOfScalar(doc, msg.hopsize, listAFF.size());
			audio_descriptor.appendChild(sos_aff);

			final Element raw_aff = doc.createElementNS(Namespace.MPEG7, "Raw");
			sos_aff.appendChild(raw_aff);
			raw_aff.appendChild(doc.createTextNode(buffer_aff.toString()));
		}
	}

	private void encodeLAT(final Document doc, final Element audioSegment) {
		if (msgLAT == null) {
			return;
		}

		addScalarElem(doc, audioSegment, "LogAttackTimeType", msgLAT.lat,
				MPEG7FeatureType.LOG_ATTACK_TIME);
	}

	private void encodeTC(final Document doc, final Element audioSegment) {
		if (msgTC == null) {
			return;
		}

		addScalarElem(doc, audioSegment, "TemporalCentroidType", msgTC.temporalCentroid,
				MPEG7FeatureType.TEMPORAL_CENTROID);
	}

	private void encodeHSC(final Document doc, final Element audioSegment) {
		if (msgHSC == null) {
			return;
		}

		addScalarElem(doc, audioSegment, "HarmonicSpectralCentroidType", msgHSC.hsc,
				MPEG7FeatureType.HARMONIC_SPECTRAL_CENTROID);
	}

	private void encodeHSD(final Document doc, final Element audioSegment) {
		if (msgHSD == null) {
			return;
		}

		addScalarElem(doc, audioSegment, "HarmonicSpectralDeviationType", msgHSD.hsd,
				MPEG7FeatureType.HARMONIC_SPECTRAL_DEVIATION);
	}

	private void encodeHSS(final Document doc, final Element audioSegment) {
		if (msgHSS == null) {
			return;
		}

		addScalarElem(doc, audioSegment, "HarmonicSpectralSpreadType", msgHSS.hss,
				MPEG7FeatureType.HARMONIC_SPECTRAL_SPREAD);
	}

	private void encodeHSV(final Document doc, final Element audioSegment) {
		if (msgHSV == null) {
			return;
		}

		addScalarElem(doc, audioSegment, "HarmonicSpectralVariationType", msgHSV.hsv,
				MPEG7FeatureType.HARMONIC_SPECTRAL_VARIATION);
	}

	private void encodeSC(final Document doc, final Element audioSegment) {
		if (msgSC == null) {
			return;
		}

		addScalarElem(doc, audioSegment, "SpectralCentroidType", msgSC.spectralCentroid,
				MPEG7FeatureType.SPECTRAL_CENTROID);
	}

	private void encodeASB(final Document doc, final Element audioSegment) {

	}

	private void encodeASP(final Document doc, final Element audioSegment) {

	}

	private void addScalarElem(final Document doc, final Element audio_segment, final String type, final float value,
			final MPEG7FeatureType featureType) {
		final double[][] rawData = new double[1][1];
		rawData[0][0] = value;

		final MPEG7AudioDescriptor descriptor = new MPEG7AudioDescriptor(rawData, new HashMap<String, Object>());
		m_descriptors.put(featureType.getConfigName(), descriptor);

		if (doc != null) {
			final Element audio_descriptor = doc.createElementNS(Namespace.MPEG7, "AudioDescriptor");
			audio_descriptor.setAttributeNS(Namespace.XSI, "xsi:type", type);
			audio_segment.appendChild(audio_descriptor);

			final Element scalar = doc.createElementNS(Namespace.MPEG7, "Scalar");
			audio_descriptor.appendChild(scalar);

			scalar.appendChild(doc.createTextNode(format(value)));
		}
	}

}
