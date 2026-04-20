package com.smartcampus.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Endpoint — GET /api/v1
 *
 * This is the entry point to the Smart Campus API. It returns essential metadata
 * and a map of all primary resource collections, implementing the HATEOAS principle.
 *
 * HATEOAS (Hypermedia as the Engine of Application State) means clients can
 * navigate the entire API by following the links returned here, without needing
 * out-of-band documentation. This makes the API self-discoverable and resilient
 * to URL structure changes.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getDiscovery() {
        // Use LinkedHashMap to preserve insertion order in the JSON output
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("api", "Smart Campus Sensor & Room Management API");
        metadata.put("version", "v1.0.0");
        metadata.put("status", "operational");
        metadata.put("admin_contact", "admin@smartcampus.edu");
        metadata.put("timestamp", System.currentTimeMillis());

        // HATEOAS links — clients follow these to navigate the API
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",           "/api/v1");
        links.put("rooms",          "/api/v1/rooms");
        links.put("sensors",        "/api/v1/sensors");
        links.put("sensor_filter",  "/api/v1/sensors?type={type}");
        links.put("readings",       "/api/v1/sensors/{sensorId}/readings");
        metadata.put("_links", links);

        return Response.ok(metadata).build();
    }
}
