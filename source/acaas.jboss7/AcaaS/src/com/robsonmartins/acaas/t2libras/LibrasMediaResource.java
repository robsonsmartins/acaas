package com.robsonmartins.acaas.t2libras;

import java.util.Map;

import org.restlet.data.MediaType;

import com.robsonmartins.acaas.MediaResource;

/**
 * Servico REST para obtencao de arquivos de midia (Libras).
 * <p/>
 * <code>id</code> - ID do arquivo de midia a ser obtido.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class LibrasMediaResource extends MediaResource {

	/** Engine de transformacao de Texto para Libras. */ 
	private static TextToLibras t2libras = null;
	
	/** 
	 * Construtor.<br/>
	 * Usa implementacao {@link AccBrasilTextToLibras}.
	 */
	public LibrasMediaResource() {
		super();
		// usa implementacao AccBrasilTextToLibras
		t2libras = new AccBrasilTextToLibras(properties);
	}

	@Override
	protected MediaType getMediaType(String format) {
		MediaType mediaType = MediaType.ALL;
		// format: OGG, MP4 ou JPG
		if (format == null) { format = ""; }
		if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("image/jpeg")) {
			mediaType = MediaType.IMAGE_JPEG;
		} else if (format.equalsIgnoreCase("mp4") || format.equalsIgnoreCase("video/mp4")) {
			mediaType = MediaType.VIDEO_MP4;
		} else { // OGG
			mediaType = MediaType.register("video/ogg", "Arquivo de Video OGG");
		}
		return mediaType;
	}

	@Override
	protected byte[] getMediaData(Map<String, String> params) throws Exception {
		String filename = params.get("filename");
		String lang = params.get("lang");
		String format = params.get("format");
		return t2libras.getMedia(filename, lang, format);
	}
}
