package com.robsonmartins.acaas.util;

import java.util.Arrays;
import java.util.List;

public enum AudioFormats {
	
	WAVE("WAV","PCM","LPCM"),
	FLAC("FLAC"),
	MPEG("MPEG","MPG","MPA","MP2","MP3");
	
	private String[] desc;
	
	public String[] toStrings() {
		return desc;
	}

	public List<String> toStringList() {
		return Arrays.asList(desc);
	}

	private AudioFormats(String... desc) {
		this.desc = desc;
	}
}
