package com.robsonmartins.acaas.util;

import java.nio.ByteOrder;

public class AudioBuffer {

	private byte[] samples;
	private int numChannels;
	private int sampleRate;
	private int bitsPerSample;
	private ByteOrder byteOrder;
	
	public AudioBuffer(){ }
	
	public AudioBuffer(int sampleRate, int bitsPerSample,
			int numChannels, ByteOrder byteOrder){
		
		this.sampleRate = sampleRate;
		this.bitsPerSample = bitsPerSample;
		this.numChannels = numChannels;
		this.byteOrder = byteOrder;
	}

	public byte[] getSamples() {
		return samples;
	}

	public void setSamples(byte[] samples) {
		this.samples = samples;
	}

	public int getNumChannels() {
		return numChannels;
	}

	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getBitsPerSample() {
		return bitsPerSample;
	}

	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}
	
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}

	public int getBytesPerSample() {
		return bitsPerSample / 8;
	}
}
