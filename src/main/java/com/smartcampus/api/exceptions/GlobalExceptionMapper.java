package com.smartcampus.api.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global safety-net mapper: catches any unexpected Throwable not handled by
 * a more specific mapper. CRITICALLY, it re-throws WebApplicationException
 * so JAX-RS built-in exceptions (404, 400, etc.) are NOT swallowed into 500s.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Re-route JAX-RS built-in exceptions so they are handled correctly
        // by the framework (e.g., NotFoundException -> 404, BadRequestException -> 400)
        if (exception instanceof WebApplicationException) {
            WebApplicationException jaxRsEx = (WebApplicationException) exception;
            Response original = jaxRsEx.getResponse();
            // Wrap with a clean JSON body instead of a raw HTML error page
            String msg = jaxRsEx.getMessage() != null ? jaxRsEx.getMessage() : "Resource not found";
            ErrorMessage error = new ErrorMessage(msg, original.getStatus());
            return Response.status(original.getStatus())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        }

        // Log the actual stack trace server-side ONLY - never expose it to the client
        LOGGER.log(Level.SEVERE, "Unhandled internal server error intercepted", exception);

        // Return a safe, generic error message with no implementation details
        ErrorMessage error = new ErrorMessage(
                "An unexpected internal server error occurred. Please contact support.", 500);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
