package com.robsonmartins.acaas.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteOrder;
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
import javax.sound.sampled.TargetDataLine;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.Player;

import org.apache.commons.io.IOUtils;

import com.sun.media.sound.WaveFileReader;
import com.sun.media.sound.WaveFileWriter;

public class AudioProcessor {
	
	public static AudioBuffer mpaToPcm(AudioBuffer data) throws Exception {
		return mpaToPcm(data.getSamples());
	}

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
	
	public static AudioBuffer wavToPcm(AudioBuffer data) throws Exception {
		return wavToPcm(data.getSamples());
	}

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

	public static AudioBuffer flacToPcm(AudioBuffer data) throws Exception {
		return flacToPcm(data.getSamples());
	}

	public static AudioBuffer flacToPcm(byte[] data) throws Exception {
		ByteArrayInputStream flacDataIStream = new ByteArrayInputStream(data);
		FlacToPcmDecoder decoder = new FlacToPcmDecoder();
		AudioBuffer result = decoder.decode(flacDataIStream);
		return result;
	}

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
		encoder.addSamples(samples, samples.length);
		encoder.encodeSamples(samples.length, true);

		AudioBuffer result = new AudioBuffer(data.getSampleRate(), data.getBitsPerSample(),
				data.getNumChannels(), data.getByteOrder());
		result.setSamples(output.toByteArray());
		return result;
	}

	public static void playMPA(AudioBuffer data) throws Exception {
		playMPA(data.getSamples());
	}

	public static void playMPA(byte[] data) throws Exception {
		Player player = new Player(new ByteArrayInputStream(data));
		player.play();
		player.close();
	}
	
	public static void playWav(AudioBuffer data) throws Exception {
		playWav(data.getSamples());
	}

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
	
	public static void playFlac(AudioBuffer data) throws Exception {
		playFlac(data.getSamples());
	}

	public static void playFlac(byte[] data) throws Exception {
		playWav(pcmToWav(flacToPcm(data)));
	}
	
	private static long timeToCapture; 
	
	public static AudioBuffer captureAudio(int sampleRate, int bitsPerSample,
			int numChannels, ByteOrder byteOrder) throws Exception {
		
		AudioBuffer result = new AudioBuffer(sampleRate, bitsPerSample,
				numChannels, byteOrder);
		
		byte tempBuffer[] = new byte[10240];
		ByteArrayOutputStream data = new ByteArrayOutputStream(1024);

		AudioFormat format =
			new AudioFormat(sampleRate, bitsPerSample, numChannels,
					false, byteOrder == ByteOrder.BIG_ENDIAN);
		TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
		microphone.open(format);
		microphone.start();
		
		timeToCapture = System.currentTimeMillis();
		do {
			int cnt = microphone.read(tempBuffer, 0, tempBuffer.length);
			if (cnt > 0) {
				data.write(tempBuffer, 0, cnt);
			}
		} while (!isSilence(data));
		
		 data.close();
		 result.setSamples(data.toByteArray());
		 return result;
	}

	private static boolean isSilence(ByteArrayOutputStream data) {
		return (System.currentTimeMillis() >= timeToCapture + 2000) ;
	}
	
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

	public static AudioBuffer genSin(int sampleRate, int bitsPerSample,
			int len, int nCycles, ByteOrder byteOrder) {
		
		int[] intData = genSin(len, bitsPerSample, nCycles);
		byte[] byteData = intArrayToByteArray(intData, bitsPerSample, byteOrder);
		AudioBuffer result = new AudioBuffer(sampleRate, bitsPerSample, 1, byteOrder);
		result.setSamples(byteData);
		return result;
	}
	
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

	public static void writeFile(String filename, byte[] data) throws Exception {
		FileOutputStream fos = new FileOutputStream(filename);
		fos.write(data);
		fos.close();
	}
	
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
	
	protected static int[] byteArrayToIntArray(byte[] in,
			int bitsPerSample, ByteOrder byteOrder) {
		
		int bytesPerSample = bitsPerSample / 8;
		int size = in.length / bytesPerSample;
		int[] out = new int[size];
		int inIdx = 0;
		for (int outIdx = 0; outIdx < out.length; outIdx++) {
			byte[] bytes = new byte[bytesPerSample];
			for (int byteIdx = 0; byteIdx < bytesPerSample; byteIdx++) {
				bytes[byteIdx] = in[inIdx];
				inIdx++;
			}
			out[outIdx] = byteToInt(bytes, bytesPerSample, byteOrder);
		}
		return out;
	}

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
}
