package com.robsonmartins.acaas.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;

import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

/**
 * Decodificador de audio FLAC para PCM. 
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class FlacToPcmDecoder implements PCMProcessor {

	/** Buffer de saida PCM. */
	private AudioBuffer result = new AudioBuffer();
	/** Buffer interno (stream de bytes). */
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	
	/**
	 * Decodifica audio FLAC para PCM.
	 * @param in Dados de audio no formato FLAC.
	 * @return Buffer de audio em formato PCM (RAW).
	 * @throws IOException
	 */
	public AudioBuffer decode(InputStream in) throws IOException {
        FLACDecoder decoder = new FLACDecoder(in);
        decoder.addPCMProcessor(this);
        decoder.decode();
        result.setSamples(buffer.toByteArray());
		return result;
	}
	
	/**
	 * Called when each data frame is decompressed.
	 * @param pcm The decompressed PCM data.
	 */
	@Override
	public void processPCM(ByteData pcm) {
		buffer.write(pcm.getData(), 0, pcm.getLen());
	}

	/**
	 * Called when StreamInfo read.
	 * @param info The FLAC stream info metadata block.
	 */ 
	@Override
	public void processStreamInfo(StreamInfo info) {
		AudioFormat format = info.getAudioFormat();
		result.setBitsPerSample(format.getSampleSizeInBits());
		result.setByteOrder(format.isBigEndian() ? 
				ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
		result.setNumChannels(format.getChannels());
		result.setSampleRate((int)format.getSampleRate());
	}
}
