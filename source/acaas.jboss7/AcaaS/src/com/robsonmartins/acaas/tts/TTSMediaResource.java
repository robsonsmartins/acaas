package com.robsonmartins.acaas.tts;

import java.util.Map;

import org.restlet.data.MediaType;

import com.robsonmartins.acaas.MediaResource;

/**
 * Servico REST para obtencao de arquivos de midia (Texto para Fala).
 * <p/>
 * <code>id</code> - ID do arquivo de midia a ser obtido.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class TTSMediaResource extends MediaResource {

	/** Engine de transformacao de Texto para Fala. */ 
	private static TextToSpeech tts = null;
	
	/** 
	 * Construtor.<br/>
	 * Usa implementacao {@link GoogleTextToSpeech}.
	 */
	public TTSMediaResource() {
		super();
		// usa implementacao GoogleTextToSpeech
		tts = new GoogleTextToSpeech(fetcher);
	}
	
	@Override
	protected MediaType getMediaType(String format) {
		MediaType mediaType = MediaType.AUDIO_WAV;
		if (format == null) { format = ""; }
		if (format.equalsIgnoreCase("flac") || format.equalsIgnoreCase("audio/x-flac")) {
			mediaType = MediaType.register("audio/x-flac", "FLAC Audio");
		} else { // WAV
			mediaType = MediaType.AUDIO_WAV;
		}
		return mediaType;
	}

	@Override
	protected byte[] getMediaData(Map<String, String> params) throws Exception {
		String filename = params.get("filename");
		String lang = params.get("lang");
		String format = params.get("format");
		return tts.getMedia(filename, lang, format);
	}
	
	
}
