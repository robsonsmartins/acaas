package com.robsonmartins.acaas.stt;

public interface SpeechToText {

	public String convert(byte[] speech, String format, String language) throws Exception;
	public String convert(byte[] speech) throws Exception;
	
}
