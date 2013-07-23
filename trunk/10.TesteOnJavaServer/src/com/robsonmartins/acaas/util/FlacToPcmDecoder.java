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

public class FlacToPcmDecoder implements PCMProcessor {

	private AudioBuffer result = new AudioBuffer();
	private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	
	public AudioBuffer decode(InputStream in) throws IOException {
        FLACDecoder decoder = new FLACDecoder(in);
        decoder.addPCMProcessor(this);
        decoder.decode();
        result.setSamples(buffer.toByteArray());
		return result;
	}
	
	@Override
	public void processPCM(ByteData pcm) {
		buffer.write(pcm.getData(), 0, pcm.getLen());
	}

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
