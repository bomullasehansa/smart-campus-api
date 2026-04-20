package com.smartcampus.api.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity with a JSON body.
 * Triggered when a client tries to POST a Sensor with a roomId that does not exist.
 */
@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorMessage error = new ErrorMessage(exception.getMessage(), 422);
        return Response.status(422) // 422 Unprocessable Entity
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
