package com.robsonmartins.acaas.util;

import java.util.Arrays;
import java.util.List;

/**
 * Enumera os Formatos dos arquivos de audio suportados.
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public enum AudioFormats {
	
	/** Formatos audio/wav. */
	WAVE("wav","pcm","lpcm", "audio/wav"),
	/** Formatos audio/x-flac. */
	FLAC("flac", "audio/x-flac"),
	/** Formatos audio/mpeg. */
	MPEG("mpeg","mpg","mpa","mp2","mp3", "audio/mpeg");
	
	private String[] desc;
	
	/**
	 * Retorna os codecs associados ao formato de audio.
	 * @return Lista com descritivo de codecs associados.
	 */
	public String[] toStrings() {
		return desc;
	}

	/**
	 * Retorna os codecs associados ao formato de audio, 
	 *   como objeto {@link List}.
	 * @return Lista com descritivo de codecs associados.
	 */
	public List<String> toStringList() {
		return Arrays.asList(desc);
	}

	private AudioFormats(String... desc) {
		this.desc = desc;
	}
}
