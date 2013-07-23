package com.robsonmartins.acaas.t2libras;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class T2LibrasVideoResource extends ServerResource {

	private static final TextToLibras t2libras = new AccBrasilTextToLibras(null);
	
	@Get
	public Representation getVideo() {
		String video = getAttribute("video");
		return getVideo(video);
	}
	
	private Representation getVideo(String filename) {
		Representation result = null;
		byte[] data = null;
		try {
			if (filename == null || filename == "") {
				throw new NullPointerException("Attribute \"video\" is required.");
			}
			data = t2libras.getVideo(filename);
			if (data == null) {
				throw new NullPointerException(filename + " not found.");
			}
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		result = new ByteArrayRepresentation(data);
		result.setMediaType(MediaType.register("video/ogg", "Arquivo de Video OGG"));
		return result;
	}
	
}
