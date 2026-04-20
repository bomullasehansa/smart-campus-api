package com.smartcampus.api.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

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
