package com.robsonmartins.acaas;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Resource;
import org.restlet.service.StatusService;

/**
 * Manipula status dos servicos REST do AcaaS (Acessibilidade como um Servico).
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class AcaaSStatusService extends StatusService {
	
	/**
	 * Returns a status for a given exception or error.
	 * By default it returns an Status.SERVER_ERROR_INTERNAL status and logs a severe message.
	 * In order to customize the default behavior, this method can be overridden.
	 * @param throwable The exception or error caught.
	 * @param resource The parent resource.
	 * @return The representation of the given status.
	 */
	@Override
	public Status getStatus(Throwable throwable, Resource resource) {
	 
		Status ret = null;
	    Throwable cause = throwable.getCause();
        Status status = super.getStatus(throwable, resource);
	    
	    if (cause == null) {
	        ret = new Status(status, throwable.getMessage());
	    } else {
	        ret = new Status(status, cause.getMessage());
	    }
	    return ret;
	}

	/**
	 * Returns a representation for the given status.
	 * In order to customize the default representation, this method can be overridden.
	 * It returns null by default.
	 * @param status The status to represent.
	 * @param request The request handled.
	 * @param response The response updated.
	 * @return The representation of the given status.
	 */
	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
	    String ret = "";
	    if (status.getDescription() != null) {
	        ret = status.getDescription();
	    } else {
	        ret = "unknown error";
	    }
	    return new StringRepresentation(ret, MediaType.TEXT_PLAIN);
	}	

}
