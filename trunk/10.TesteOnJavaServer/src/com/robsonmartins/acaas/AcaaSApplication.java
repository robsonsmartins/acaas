package com.robsonmartins.acaas;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.robsonmartins.acaas.stt.STTResource;
import com.robsonmartins.acaas.t2libras.T2LibrasResource;
import com.robsonmartins.acaas.t2libras.T2LibrasVideoResource;
import com.robsonmartins.acaas.tts.TTSAudioResource;
import com.robsonmartins.acaas.tts.TTSResource;

public class AcaaSApplication extends Application {

	@Override
	public synchronized Restlet createInboundRoot() {

        setStatusService(new AcaaSStatusService());

        Router router = new Router(getContext());
        router.attach("/tts", TTSResource.class);
        router.attach("/tts/audio/{audio}", TTSAudioResource.class);

        router.attach("/stt", STTResource.class);

        router.attach("/t2libras", T2LibrasResource.class);
        router.attach("/t2libras/video/{video}", T2LibrasVideoResource.class);
                
        return router;
	}
	
}
