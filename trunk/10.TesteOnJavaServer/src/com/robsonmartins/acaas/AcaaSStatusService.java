package com.robsonmartins.acaas;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Resource;
import org.restlet.service.StatusService;

public class AcaaSStatusService extends StatusService {
	
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
