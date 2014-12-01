package com.robsonmartins.acaas.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javaFlacEncoder.FLACEncoder;
import javaFlacEncoder.FLACOutputStream;
import javaFlacEncoder.FLACStreamOutputStream;
import javaFlacEncoder.StreamConfiguration;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.Player;

import org.apache.commons.io.IOUtils;

import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFileWriter;

/**
 * Manipulador de conteudos de Audio. 
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class AudioProcessor {
	
	/**
	 * Converte audio no formato MPEG para RAW PCM.
	 * @param data {@link AudioBuffer} contendo dados de audio no formato MPEG.
	 * @return {@link AudioBuffer} com dados RAW PCM.
	 * @throws Exception
	 */
	public static AudioBuffer mpaToPcm(AudioBuffer data) throws Exception {
		return mpaToPcm(data.getSamples());
	}

	/**
	 * Converte audio no formato MPEG para RAW PCM.
	 * @param data Array de bytes contendo dados de audio no formato MPEG.
	 * @return {@link AudioBuffer} com dados RAW PCM.
	 * @throws Exception
	 */
	public static AudioBuffer mpaToPcm(byte[] data) throws Exception {
		AudioBuffer result = new AudioBuffer();
		ByteArrayOutputStream pcm = new ByteArrayOutputStream();
		Bitstream bitstream = new Bitstream(new ByteArrayInputStream(data));
		boolean done = false;
		Decoder decoder = new Decoder();
		while (!done) {
			Header frameHeader = bitstream.readFrame();
			if (frameHeader == null) {
				done = true;
			} else {
				SampleBuffer buffer = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
				if (result.getSampleRate() == 0) {
					result.setSampleRate(buffer.getSampleFrequency());
				}
				if (result.getNumChannels() == 0) {
					result.setNumChannels(buffer.getChannelCount());
				}
				if (result.getBitsPerSample() != 16) {
					result.setBitsPerSample(16);
				}
				if (result.getByteOrder() != ByteOrder.LITTLE_ENDIAN) {
					result.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				}
				short[] pcmBuffer = buffer.getBuffer();
				int pcmLen = buffer.getBufferLength();
				if (pcmBuffer == null || pcmLen <= 0){
					done = true;
				} else {
					for (int i = 0; i < pcmLen; i++) {
						byte[] bytes = shortToByte(pcmBuffer[i], result.getByteOrder());
						pcm.write(bytes[0]);
						pcm.write(bytes[1]);
					}
				}
			}
			bitstream.closeFrame();
		}
		pcm.close();
		result.setSamples(pcm.toByteArray());
		return result;
	}

	/**
	 * Converte audio no formato WAV/RIFF para RAW PCM.
	 * @param data {@link AudioBuffer} contendo dados de audio no formato WAV.
	 * @return {@link AudioBuffer} com dados RAW PCM.
	 * @throws Exception
	 */
	public static AudioBuffer wavToPcm(AudioBuffer data) throws Exception {
		return wavToPcm(data.getSamples());
	}

	/**
	 * Converte audio no formato WAV/RIFF para RAW PCM.
	 * @param data Array de bytes contendo dados de audio no formato WAV.
	 * @return {@link AudioBuffer} com dados RAW PCM.
	 * @throws Exception
	 */
	public static AudioBuffer wavToPcm(byte[] data) throws Exception {
		WaveFileReader wav = new WaveFileReader();
		ByteArrayInputStream wavDataIStream = new ByteArrayInputStream(data);
		ByteArrayOutputStream pcmDataOStream = new ByteArrayOutputStream();
		AudioInputStream wavIStream =
			wav.getAudioInputStream(wavDataIStream);
		AudioFormat format = wavIStream.getFormat();
		IOUtils.copy(wavIStream, pcmDataOStream);
		AudioBuffer result = new AudioBuffer((int)format.getSampleRate(),
				format.getSampleSizeInBits(), format.getChannels(), 
				format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
		result.setSamples(pcmDataOStream.toByteArray());
		return result;
	}

	/**
	 * Converte audio no formato FLAC para RAW PCM.
	 * @param data {@link AudioBuffer} contendo dados de audio no formato FLAC.
	 * @return {@link AudioBuffer} com dados RAW PCM.
	 * @throws Exception
	 */
	public static AudioBuffer flacToPcm(AudioBuffer data) throws Exception {
		return flacToPcm(data.getSamples());
	}

	/**
	 * Converte audio no formato FLAC para RAW PCM.
	 * @param data Array de bytes contendo dados de audio no formato FLAC.
	 * @return {@link AudioBuffer} com dados RAW PCM.
	 * @throws Exception
	 */
	public static AudioBuffer flacToPcm(byte[] data) throws Exception {
		ByteArrayInputStream flacDataIStream = new ByteArrayInputStream(data);
		FlacToPcmDecoder decoder = new FlacToPcmDecoder();
		AudioBuffer result = decoder.decode(flacDataIStream);
		return result;
	}

	/**
	 * Converte audio RAW PCM para o formato WAV/RIFF.
	 * @param data {@link AudioBuffer} contendo dados RAW PCM.
	 * @return {@link AudioBuffer} contendo audio no formato WAV.
	 * @throws Exception
	 */
	public static AudioBuffer pcmToWav(AudioBuffer data) throws Exception {
		WaveFileWriter wav = new WaveFileWriter();
		AudioFormat format = 
			new AudioFormat(data.getSampleRate(), data.getBitsPerSample(),
					data.getNumChannels(), true,
					data.getByteOrder() == ByteOrder.BIG_ENDIAN);
		ByteArrayInputStream pcmDataIStream = 
			new ByteArrayInputStream(data.getSamples());
		AudioInputStream pcmIStream =
			new AudioInputStream(pcmDataIStream, format,
					data.getSamples().length);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		wav.write(pcmIStream, AudioFileFormat.Type.WAVE, output);
		AudioBuffer result = new AudioBuffer(data.getSampleRate(), data.getBitsPerSample(),
				data.getNumChannels(), data.getByteOrder());
		result.setSamples(output.toByteArray());
		return result;
	}

	/**
	 * Converte audio RAW PCM para o formato FLAC.
	 * @param data {@link AudioBuffer} contendo dados RAW PCM.
	 * @return {@link AudioBuffer} contendo audio no formato FLAC.
	 * @throws Exception
	 */
	public static AudioBuffer pcmToFlac(AudioBuffer data) throws Exception {
		FLACEncoder encoder = new FLACEncoder();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FLACOutputStream flacOutput = new FLACStreamOutputStream(output);
		StreamConfiguration flacConfig = new StreamConfiguration();
		flacConfig.setBitsPerSample(data.getBitsPerSample());
		flacConfig.setChannelCount(data.getNumChannels());
		flacConfig.setSampleRate(data.getSampleRate());

		encoder.setStreamConfiguration(flacConfig);
		encoder.setOutputStream(flacOutput);
		encoder.openFLACStream();

		int[] samples = byteArrayToIntArray(data.getSamples(),
				data.getBitsPerSample(), data.getByteOrder());
		encoder.addSamples(samples, samples.length / data.getNumChannels());
		encoder.encodeSamples(samples.length / data.getNumChannels(), true);

		AudioBuffer result = new AudioBuffer(data.getSampleRate(), data.getBitsPerSample(),
				data.getNumChannels(), data.getByteOrder());
		result.setSamples(output.toByteArray());
		return result;
	}

	/**
	 * Reproduz audio no formato MPEG.
	 * @param data {@link AudioBuffer} contendo dados de audio no formato MPEG.
	 * @throws Exception
	 */
	public static void playMPA(AudioBuffer data) throws Exception {
		playMPA(data.getSamples());
	}

	/**
	 * Reproduz audio no formato MPEG.
	 * @param data Array de bytes contendo dados de audio no formato MPEG.
	 * @throws Exception
	 */
	public static void playMPA(byte[] data) throws Exception {
		Player player = new Player(new ByteArrayInputStream(data));
		player.play();
		player.close();
	}
	
	/**
	 * Reproduz audio no formato WAV/RIFF.
	 * @param data {@link AudioBuffer} contendo dados de audio no formato WAV.
	 * @throws Exception
	 */
	public static void playWav(AudioBuffer data) throws Exception {
		playWav(data.getSamples());
	}

	/**
	 * Reproduz audio no formato WAV/RIFF.
	 * @param data Array de bytes contendo dados de audio no formato WAV.
	 * @throws Exception
	 */
	public static void playWav(byte[] data) throws Exception {
		ByteArrayInputStream wavData = new ByteArrayInputStream(data);
		AudioInputStream wavIStream = AudioSystem.getAudioInputStream(wavData);
		AudioFormat format = wavIStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceLine.open(format);
        sourceLine.start();
        byte[] wavBuffer = IOUtils.toByteArray(wavIStream);
        sourceLine.write(wavBuffer, 0, wavBuffer.length);
        sourceLine.drain();
        sourceLine.close();
	}
	
	/**
	 * Reproduz audio no formato FLAC.
	 * @param data {@link AudioBuffer} contendo dados de audio no formato FLAC.
	 * @throws Exception
	 */
	public static void playFlac(AudioBuffer data) throws Exception {
		playFlac(data.getSamples());
	}

	/**
	 * Reproduz audio no formato FLAC.
	 * @param data Array de bytes contendo dados de audio no formato FLAC.
	 * @throws Exception
	 */
	public static void playFlac(byte[] data) throws Exception {
		playWav(pcmToWav(flacToPcm(data)));
	}
	
	/**
	 * Normaliza audio (maximiza picos).
	 * @param data Buffer de audio RAW PCM.
	 * @return Buffer de audio normalizado.
	 */
	public static AudioBuffer normalize(AudioBuffer data) {
		AudioBuffer result = new AudioBuffer(data.getSampleRate(), data.getBitsPerSample(),
				data.getNumChannels(), data.getByteOrder());
		int[] buffer = byteArrayToIntArray(data.getSamples(),
				data.getBitsPerSample(), data.getByteOrder());
		float max = 0.0f;
		for (int i = 0; i < buffer.length; i++) {
			max = Math.max(max, Math.abs(buffer[i]));
		}
		float newPeak = (float)(Math.pow(2.0f, data.getBitsPerSample()) / 2.0f - 1.0f);
		float scale = (float)(Math.abs(newPeak / ((max > 1.0) ? (max - 2.0) : 1.0)));
		for (int i = 0; i < buffer.length; i++) {
			long sample = (long)(buffer[i] * scale);
			if (sample > newPeak) {
				buffer[i] = (int)newPeak;
			} else if (sample < -newPeak) {
				buffer[i] = (int)(-newPeak);
			} else {
				buffer[i] = (int)(sample);
			}
		}
		result.setSamples(intArrayToByteArray(buffer,
				data.getBitsPerSample(), data.getByteOrder()));
		return result;
	}
	
	/**
	 * Divide um audio RAW PCM em pedacos, detectando silencio.
	 * @param data Dados do audio RAW PCM.
	 * @param lenSilence Duracao do silencio, em ms.
	 * @param dbSilence Nivel do silencio, em dB (&gt;= 0dB: peek).
	 * @return Pedacos do audio RAW PCM original.
	 */
	public static AudioBuffer[] tokenize(AudioBuffer data, int lenSilence, double dbSilence) {
		List<AudioBuffer> result = new ArrayList<AudioBuffer>();
		int[] buffer = byteArrayToIntArray(data.getSamples(),
				data.getBitsPerSample(), data.getByteOrder());
		int silenceSamples = 
			data.getSampleRate() * lenSilence * data.getNumChannels() / 1000;
		double peek = Math.pow(2.0, data.getBitsPerSample()) / 2.0 - 1.0;
		int startIdx = 0, sampleIdx = 0, endIdx = silenceSamples - 1;
		boolean foundSilence = false, lastTokenSilence = false;
		while (startIdx < buffer.length && endIdx <= buffer.length) {
			// procura uma amostra de silencio na janela
			int silencePoint = searchSilencePoint(buffer, startIdx, endIdx, peek, dbSilence);
			if (silencePoint >= 0) {
				// achou uma amostra de silencio
				// altera janela para comecar nessa amostra
				startIdx = silencePoint;
				endIdx = startIdx + silenceSamples - 1;
				// procura uma amostra nao-silencio na janela
				int noSilencePoint = searchNoSilencePoint(buffer, startIdx, endIdx, peek, dbSilence);
				if (noSilencePoint >= 0) {
					// achou uma amostra nao-silencio na janela
					// altera janela para comecar depois dessa amostra
					startIdx = noSilencePoint + 1;
					endIdx = startIdx + silenceSamples - 1;
					foundSilence = false;
				} else {
					// nao achou uma amostra nao-silencio na janela
					// entao janela e' inteira de silencio
					foundSilence = true;
				}
			} else {
				// nao achou uma amostra silencio na janela
				// entao janela nao e' de silencio
				foundSilence = false;
				startIdx = endIdx + 1;
				endIdx = startIdx + silenceSamples - 1;
			}
			// ultrapassou fim do buffer sem encontrar janela inteira de silencio
			if (!foundSilence && endIdx >= buffer.length) {
				// considera que apos o buffer ha' silencio
				startIdx = buffer.length - 1;
				foundSilence = true;
			}
			// se encontrou janela de silencio
			if (foundSilence) {
				// copia token ate' inicio da janela de silencio
				// se apos o ultimo token nao havia silencio
				// isso impede gravar tokens contendo somente silencio
				if (!lastTokenSilence && sampleIdx < startIdx) {
					AudioBuffer token = getToken(buffer, sampleIdx, startIdx,
							data.getSampleRate (), data.getBitsPerSample(), 
							data.getNumChannels(), data.getByteOrder()); 
					result.add(token);
				}
				startIdx = endIdx + 1;
				sampleIdx = startIdx;
				endIdx = startIdx + silenceSamples - 1;
				lastTokenSilence = true;
			}
			else { lastTokenSilence = false; } // nao achou janela de silencio
		}
		return result.toArray(new AudioBuffer[0]);
	}

	/**
	 * Retorna uma onda senoidal no formato RAW PCM.
	 * @param sampleRate Taxa de amostragem desejada, em Hz.
	 * @param bitsPerSample Quantidade de bits por amostra (tipicamente 8 a 32).
	 * @param len Comprimento desejado para a onda, em numero de amostras.
	 * @param nCycles Numero de ciclos desejado.
	 * @param byteOrder Ordem de alinhamento de bytes desejado, 
	 *   para amostras com mais de 8 bits.
	 * @return {@link AudioBuffer},
	 *   contendo uma senoide normalizada, no formato RAW PCM.
	 */
	public static AudioBuffer genSin(int sampleRate, int bitsPerSample,
			int len, int nCycles, ByteOrder byteOrder) {
		
		int[] intData = genSin(len, bitsPerSample, nCycles);
		byte[] byteData = intArrayToByteArray(intData, bitsPerSample, byteOrder);
		AudioBuffer result = new AudioBuffer(sampleRate, bitsPerSample, 1, byteOrder);
		result.setSamples(byteData);
		return result;
	}
	
	/**
	 * Retorna uma onda senoidal no formato RAW PCM.
	 * @param len Comprimento desejado para a onda, em numero de amostras.
	 * @param bitsPerSample Quantidade de bits por amostra (tipicamente 8 a 32).
	 * @param nCycles Numero de ciclos desejado.
	 * @return Array de amostras (signed, 32 bits),
	 *   contendo uma senoide normalizada, no formato RAW PCM.
	 */
	public static int[] genSin(int len, int bitsPerSample, int nCycles) {
		int[] buffer = new int[len];
		final double ANGLE_CONSTANT = 2.0 * Math.PI;
		final double MAX_VALUE = Math.pow(2, bitsPerSample) / 2 - 1;
		for (int i = 0; i < len; i++) {
			double angle = i * ANGLE_CONSTANT * nCycles / len;
			buffer[i] = (int)(Math.sin(angle) * MAX_VALUE);
		}
		return buffer;
	}

	/**
	 * Escreve dados em um arquivo, sobrescrevendo se ja' existir.
	 * @param filename Nome do arquivo.
	 * @param data Dados a serem escritos.
	 * @throws Exception
	 */
	public static void writeFile(String filename, byte[] data) throws Exception {
		FileOutputStream fos = new FileOutputStream(filename);
		fos.write(data);
		fos.close();
	}
	
	/**
	 * Le dados a partir de um arquivo existente.
	 * @param filename Nome do arquivo.
	 * @return Dados lidos.
	 * @throws Exception
	 */
	public static byte[] readFile(String filename) throws Exception {
		FileInputStream fis = new FileInputStream(filename);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[10240];
		int nread = 0;
		do {
			nread = fis.read(buffer);
			if (nread > 0) {
				out.write(buffer);
			}
		} while (nread > 0);
		fis.close();
		return out.toByteArray();
	}
	
	/**
	 * Converte um Array de <b>int</b> para um Array de <b>byte</b>.
	 * @param in Array de origem.
	 * @param bitsPerSample Quantidade de bits por amostra (tipicamente 8 a 32).
	 * @param byteOrder Ordem de alinhamento de bytes desejado, 
	 *   para amostras com mais de 8 bits.
	 * @return Array convertido.
	 */
	protected static byte[] intArrayToByteArray(int[] in,
			int bitsPerSample, ByteOrder byteOrder) {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int bytesPerSample = bitsPerSample / 8;
		for (int i = 0; i < in.length; i++) {
			byte[] bytes = intToByte(in[i], byteOrder);
			for (int j = 0; j < bytesPerSample; j++) {
				out.write(bytes[j]);
			}
		}
		return out.toByteArray();
	}

	/**
	 * Converte um Array de <b>short</b> para um Array de <b>byte</b>.
	 * @param in Array de origem.
	 * @param bitsPerSample Quantidade de bits por amostra (tipicamente 8 a 16).
	 * @param byteOrder Ordem de alinhamento de bytes desejado, 
	 *   para amostras com mais de 8 bits.
	 * @return Array convertido.
	 */
	protected static byte[] shortArrayToByteArray(short[] in,
			int bitsPerSample, ByteOrder byteOrder) {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int bytesPerSample = bitsPerSample / 8;
		for (int i = 0; i < in.length; i++) {
			byte[] bytes = shortToByte(in[i], byteOrder);
			for (int j = 0; j < bytesPerSample; j++) {
				out.write(bytes[j]);
			}
		}
		return out.toByteArray();
	}

	/**
	 * Converte um Array de <b>byte</b> para um Array de <b>int</b>.
	 * @param in Array de origem.
	 * @param bitsPerSample Quantidade de bits por amostra (tipicamente 8 a 32).
	 * @param byteOrder Ordem de alinhamento de bytes,
	 *   para amostras com mais de 8 bits.
	 * @return Array convertido.
	 */
	protected static int[] byteArrayToIntArray(byte[] in,
			int bitsPerSample, ByteOrder byteOrder) {
		
		int bytesPerSample = bitsPerSample / 8;
		int size = in.length / bytesPerSample;
		int[] out = new int[size];
		int inIdx = 0;
		for (int outIdx = 0; outIdx < out.length; outIdx++) {
			if (inIdx < in.length - (bytesPerSample - 1)) {
				byte[] bytes = new byte[bytesPerSample];
				for (int byteIdx = 0; byteIdx < bytesPerSample; byteIdx++) {
					bytes[byteIdx] = in[inIdx];
					inIdx++;
				}
				out[outIdx] = byteToInt(bytes, bytesPerSample, byteOrder);
			}
		}
		return out;
	}

	/**
	 * Converte um <b>int</b> para um Array de <b>byte</b>.
	 * @param in Amostra de origem.
	 * @param byteOrder Ordem de alinhamento de bytes.   
	 * @return Array convertido (4 bytes).
	 */
	protected static byte[] intToByte(int in, ByteOrder byteOrder) {
		byte[] out = new byte[4];
		int shift = 0;
		for (int i = 0; i < out.length; i++) {
			if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
				shift = 8 * i;
			} else {
				shift = 8 * (out.length - 1 - i);
			}
			out[i] = (byte)(in >> shift);
		}
		return out;
	}

	/**
	 * Converte um <b>short</b> para um Array de <b>byte</b>.
	 * @param in Amostra de origem.
	 * @param byteOrder Ordem de alinhamento de bytes.   
	 * @return Array convertido (2 bytes).
	 */
	protected static byte[] shortToByte(short in, ByteOrder byteOrder) {
		byte[] out = new byte[2];
		int shift = 0;
		for (int i = 0; i < out.length; i++) {
			if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
				shift = 8 * i;
			} else {
				shift = 8 * (out.length - 1 - i);
			}
			out[i] = (byte)(in >> shift);
		}
		return out;
	}
	
	/**
	 * Converte um Array de <b>bytes</b> para um <b>int</b>.
	 * @param in Array de origem.
	 * @param bytesPerSample Quantidade de bytes por amostra (tipicamente 1 a 4).
	 * @param byteOrder Ordem de alinhamento de bytes,
	 *   para amostras com mais de 1 byte.
	 * @return Amostra convertida.
	 */
	protected static int byteToInt(byte[] in,
			int bytesPerSample, ByteOrder byteOrder) {
		
		int out = 0;
		if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
			for (int byteIdx = bytesPerSample - 1; byteIdx >= 0; byteIdx--) {
				out <<= 8;
				out |= (in[byteIdx] & 0xff);
			}
		} else {
			for (int byteIdx = 0; byteIdx < bytesPerSample; byteIdx++) {
				out <<= 8;
				out |= (in[byteIdx] & 0xff);
			}
		}
		switch (bytesPerSample) {
			case 1 : return (byte ) out;
			case 2 : return (short) out;
			case 4 : return (int  ) out;
			default: return         out; 
		}
	}
	
	/**
	 * Busca uma amostra de silencio em um buffer RAW PCM de audio.
	 * @param data Buffer com audio RAW PCM.
	 * @param start Indice de inicio da pesquisa (0 = inicio do buffer).
	 * @param end Indice de fim da pesquisa (0 = inicio do buffer).
	 * @param peek Valor de pico de uma amostra do buffer.
	 * @param dbSilence Nivel de silencio, em dB (0 = valor de pico).
	 * @return Indice da amostra de silencio no buffer, ou -1 se nao encontrado silencio.
	 */
	private static int searchSilencePoint(int[] data, int start, int end, double peek, double dbSilence) {
		return searchPoint(true, data, start, end, peek, dbSilence);
	}

	/**
	 * Busca uma amostra sem silencio (com sinal) em um buffer RAW PCM de audio.
	 * @param data Buffer com audio RAW PCM.
	 * @param start Indice de inicio da pesquisa (0 = inicio do buffer).
	 * @param end Indice de fim da pesquisa (0 = inicio do buffer).
	 * @param peek Valor de pico de uma amostra do buffer.
	 * @param dbSilence Nivel de silencio, em dB (0 = valor de pico).
	 * @return Indice da amostra de nao-silencio no buffer, ou -1 se trecho contem apenas silencio.
	 */
	private static int searchNoSilencePoint(int[] data, int start, int end, double peek, double dbSilence) {
		return searchPoint(false, data, start, end, peek, dbSilence);
	}

	/**
	 * Busca uma amostra com criterio de silencio (ou nao-silencio) em um buffer RAW PCM de audio.
	 * @param silence Se true, busca por silencio; se false, busca por nao-silencio. 
	 * @param data Buffer com audio RAW PCM.
	 * @param start Indice de inicio da pesquisa (0 = inicio do buffer).
	 * @param end Indice de fim da pesquisa (0 = inicio do buffer).
	 * @param peek Valor de pico de uma amostra do buffer.
	 * @param dbSilence Nivel de silencio, em dB (0 = valor de pico).
	 * @return Indice da amostra pesquisada no buffer, ou -1 se nao encontrada.
	 */
	private static int searchPoint(boolean silence, int[] data, int start, int end, double peek, double dbSilence) {
		double dbRMS = 0.0f;
		for (int i = start; i <= end; i++) {
			if (i >= data.length) { break; }
			dbRMS = 10.0 * Math.log10(Math.abs(data[i]) / peek);
			if ((silence && dbRMS < dbSilence) || (!silence && dbRMS > dbSilence)) { 
				return i;
			}				
		}
		return -1;
	}
	
	/**
	 * Copia um trecho de um buffer de audio RAW PCM.
	 * @param data Buffer com audio RAW PCM. 
	 * @param start Indice de inicio do trecho (0 = inicio do buffer).
	 * @param end Indice de fim do trecho (0 = inicio do buffer).
	 * @param sampleRate Taxa de amostragem do buffer, em Hz.
	 * @param bitsPerSample Quantidade de bits por amostra (tipicamente 8 a 32).
	 * @param numChannels Numero de canais de audio entrelacados (interleaved).
	 * @param byteOrder Ordem de alinhamento de bytes,
	 *   para amostras com mais de 8 bits.
	 * @return {@link AudioBuffer} contendo o trecho copiado, com audio RAW PCM.
	 */
	private static AudioBuffer getToken(int[] data, int start, int end, int sampleRate,
			int bitsPerSample, int numChannels, ByteOrder byteOrder) {
		AudioBuffer token = 
			new AudioBuffer(sampleRate, bitsPerSample, numChannels, byteOrder);
		byte[] samples =
			intArrayToByteArray(Arrays.copyOfRange(data, start, end + 1),
				bitsPerSample, byteOrder);
		token.setSamples(samples);
		return token;
	}
}
