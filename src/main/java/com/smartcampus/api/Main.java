package com.smartcampus.api;

import com.smartcampus.api.config.SmartCampusApplication;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to launch the Grizzly server.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and providers
        final SmartCampusApplication config = new SmartCampusApplication();

        // Create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        startServer();
        System.out.println(String.format("Jersey app started with endpoints available at "
                + "%s%nHit CTRL-C to stop it...", BASE_URI));

        try {
            // Keep the server running
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
