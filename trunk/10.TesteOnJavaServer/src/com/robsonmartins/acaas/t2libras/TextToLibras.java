package com.robsonmartins.acaas.t2libras;

import org.json.JSONObject;


public interface TextToLibras {
	
	public JSONObject convert(String text, String lang) throws Exception;
	public byte[] getVideo(String filename) throws Exception;

}
