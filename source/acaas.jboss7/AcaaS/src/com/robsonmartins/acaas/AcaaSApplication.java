package com.robsonmartins.acaas;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.service.RangeService;

import com.robsonmartins.acaas.s2libras.S2LibrasResource;
import com.robsonmartins.acaas.stt.STTResource;
import com.robsonmartins.acaas.t2libras.LibrasMediaResource;
import com.robsonmartins.acaas.t2libras.T2LibrasResource;
import com.robsonmartins.acaas.tts.TTSMediaResource;
import com.robsonmartins.acaas.tts.TTSResource;

/**
 * Aplicacao REST do AcaaS (Acessibilidade como um Servico).<p/>
 * 
 * <table>
 * <tr><td><code>/acaas/tts</code>                </td><td>([in] text/plain; [out] application/json)                          </td><td>Servico de <em>Text To Speech</em> (TTS).                            </td><td>See {@link TTSResource}.        </td></tr>
 * <tr><td><code>/acaas/tts/media/{id}</code>     </td><td>([out] audio/wav*, audio/x-flac)                                   </td><td>Servico de obtencao de midia do <em>Text To Speech</em> (TTS).       </td><td>See {@link TTSMediaResource}.   </td></tr>
 * <tr><td><code>/acaas/stt</code>                </td><td>([in] audio/wav*, audio/x-flac, audio/mpeg; [out] application/json)</td><td>Servico de <em>Speech To Text</em> (STT).                            </td><td>See {@link STTResource}.        </td></tr>
 * <tr><td><code>/acaas/t2libras</code>           </td><td>([in] text/plain; [out] application/json)                          </td><td>Servico de <em>Text To Libras</em> (T2Libras).                       </td><td>See {@link T2LibrasResource}.   </td></tr>
 * <tr><td><code>/acaas/t2libras/media/{id}</code></td><td>([out] video/ogg*, video/mp4, image/jpg)                           </td><td>Servico de obtencao de midia do <em>Text To Libras</em> (T2Libras).  </td><td>See {@link LibrasMediaResource}.</td></tr>
 * <tr><td><code>/acaas/s2libras</code>           </td><td>([in] audio/wav*, audio/x-flac, audio/mpeg; [out] application/json)</td><td>Servico de <em>Speech To Libras</em> (S2Libras).                     </td><td>See {@link S2LibrasResource}.   </td></tr>
 * <tr><td><code>/acaas/s2libras/media/{id}</code></td><td>([out] video/ogg*, video/mp4, image/jpg)                           </td><td>Servico de obtencao de midia do <em>Speech To Libras</em> (S2Libras).</td><td>See {@link LibrasMediaResource}.</td></tr>
 * </table>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class AcaaSApplication extends Application {

	/**
	 * Creates a inbound root Restlet that will receive all incoming calls.
	 * In general, instances of Router, Filter or Finder classes will be used as initial application Restlet.
	 * The default implementation returns null by default. This method is intended to be overridden by subclasses.
	 * @return The inbound root Restlet.
	 */
	@Override
	public synchronized Restlet createInboundRoot() {

        setStatusService(new AcaaSStatusService());
        setRangeService(new RangeService(false));
        
        Router router = new Router(getContext());
        
        router.attach("/tts", TTSResource.class);
        router.attach("/tts/media/{id}", TTSMediaResource.class);

        router.attach("/stt", STTResource.class);

        router.attach("/t2libras", T2LibrasResource.class);
        router.attach("/t2libras/media/{id}", LibrasMediaResource.class);

        router.attach("/s2libras", S2LibrasResource.class);
        router.attach("/s2libras/media/{id}", LibrasMediaResource.class);
        
        return router;
	}
	
}
