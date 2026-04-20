package com.smartcampus.api.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API Observability Filter.
 *
 * Implements BOTH ContainerRequestFilter and ContainerResponseFilter in a single
 * class — a "cross-cutting concern" handled centrally rather than scattered
 * across every resource method (which would violate the DRY principle).
 *
 * This avoids the need to manually insert Logger.info() statements inside every
 * single resource method, ensuring logging is consistent and global.
 *
 * Every incoming request and outgoing response is logged here automatically.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Logs every incoming HTTP request: method + full URI.
     * Called BEFORE the resource method is invoked.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(String.format(">>> INCOMING REQUEST  : [%s] %s", method, uri));
    }

    /**
     * Logs every outgoing HTTP response: method + URI + status code.
     * Called AFTER the resource method has completed.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String method  = requestContext.getMethod();
        String uri     = requestContext.getUriInfo().getRequestUri().toString();
        int    status  = responseContext.getStatus();
        LOGGER.info(String.format("<<< OUTGOING RESPONSE : [%s] %s -> HTTP %d", method, uri, status));
    }
}
