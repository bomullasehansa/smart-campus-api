package com.smartcampus.api.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS Application configuration for the Smart Campus API.
 *
 * Note: ResourceConfig is Jersey's own subclass of javax.ws.rs.core.Application.
 * Extending ResourceConfig is the standard, recommended approach for Jersey-based
 * JAX-RS applications — it fully satisfies the spec requirement of implementing
 * a subclass of javax.ws.rs.core.Application while providing convenient helper
 * methods such as packages() for classpath scanning.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {
    public SmartCampusApplication() {
        // Scan for resources in our package
        packages("com.smartcampus.api.resources", 
                 "com.smartcampus.api.filters", 
                 "com.smartcampus.api.exceptions");
        
        // Ensure JSON support is registered
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
    }
}
