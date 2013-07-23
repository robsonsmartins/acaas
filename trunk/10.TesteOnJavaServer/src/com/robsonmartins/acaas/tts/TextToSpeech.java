package com.robsonmartins.acaas.tts;

import org.json.JSONObject;


public interface TextToSpeech {
	
	public JSONObject convert(String text, String language, String format) throws Exception;
	public JSONObject convert(String text) throws Exception;
	public byte[] getAudio(String text, String language, String format) throws Exception;
	public byte[] getAudio(String text) throws Exception;

}
