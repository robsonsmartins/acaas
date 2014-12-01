package com.robsonmartins.acaas.util;

import java.nio.ByteOrder;

/**
 * Buffer em memoria para dados de audio.  
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class AudioBuffer {

	private byte[] samples;
	private int numChannels;
	private int sampleRate;
	private int bitsPerSample;
	private ByteOrder byteOrder;
	
	/** Construtor. */
	public AudioBuffer(){ }
	
	/**
	 * Construtor. 
	 * @param sampleRate Taxa de amostragem, em samples/second.
	 * @param bitsPerSample Numero de bits de uma amostra.
	 * @param numChannels Numero de canais da amostragem.
	 * @param byteOrder Alinhamento de bytes de uma amostra.
	 */
	public AudioBuffer(int sampleRate, int bitsPerSample,
			int numChannels, ByteOrder byteOrder){
		
		this.sampleRate = sampleRate;
		this.bitsPerSample = bitsPerSample;
		this.numChannels = numChannels;
		this.byteOrder = byteOrder;
	}

	/**
	 * Retorna os dados (buffer) do audio.
	 * @return Dados binarios.
	 */
	public byte[] getSamples() {
		return samples;
	}

	/**
	 * Configura os dados (buffer) do audio.
	 * @param samples Dados binarios do audio.
	 */
	public void setSamples(byte[] samples) {
		this.samples = samples;
	}

	/**
	 * Retorna o numero de canais da amostragem.
	 * @return Numero de canais.
	 */
	public int getNumChannels() {
		return numChannels;
	}

	/**
	 * Configura o numero de canais da amostragem.
	 * @param numChannels Numero de canais.
	 */
	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
	}

	/**
	 * Retorna a taxa de amostragem.
	 * @return Taxa de amostragem, em samples/second.
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * Configura a taxa de amostragem.
	 * @param sampleRate Taxa de amostragem, em samples/second.
	 */
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * Retorna o numero de bits de uma amostra.
	 * @return Numero de bits.
	 */
	public int getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * Configura o numero de bits de uma amostra.
	 * @param bitsPerSample Numero de bits.
	 */
	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}
	
	/**
	 * Retorna o numero de bytes de uma amostra.
	 * @return Numero de bytes.
	 */
	public int getBytesPerSample() {
		return bitsPerSample / 8;
	}

	/**
	 * Retorna o alinhamento de bytes de uma amostra.
	 * @return Alinhamento de bytes.
	 */
	public ByteOrder getByteOrder() {
		return byteOrder;
	}

	/**
	 * Configura o alinhamento de bytes de uma amostra.
	 * @param byteOrder Alinhamento de bytes.
	 */
	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}
}
