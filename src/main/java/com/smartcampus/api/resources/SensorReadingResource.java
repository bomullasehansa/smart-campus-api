package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.ErrorMessage;
import com.smartcampus.api.exceptions.SensorUnavailableException;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;
import com.smartcampus.api.repository.DataRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.UUID;

/**
 * Sub-Resource class for managing historical SensorReadings.
 * Instantiated by SensorResource's sub-resource locator for:
 *     GET  /api/v1/sensors/{sensorId}/readings
 *     POST /api/v1/sensors/{sensorId}/readings
 *
 * This class has no @Path annotation of its own — routing is delegated
 * entirely by the parent SensorResource locator method.
 *
 * Side Effect: A successful POST updates the parent Sensor's currentValue
 * to ensure data consistency across the API.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final String sensorId;
    private final DataRepository repository = DataRepository.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the complete historical log of readings for this sensor.
     */
    @GET
    public Collection<SensorReading> getReadings() {
        return repository.getReadingsForSensor(sensorId);
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to the sensor's history.
     * Returns 201 Created on success, with the persisted reading (including generated ID/timestamp).
     * Returns 403 Forbidden if the sensor is under MAINTENANCE or OFFLINE.
     * Returns 400 Bad Request if the reading value is missing.
     *
     * Side Effect: Updates the parent Sensor's currentValue field to the new reading's value.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = repository.getSensor(sensorId)
                .orElseThrow(() -> {
                    ErrorMessage err = new ErrorMessage("Sensor with ID '" + sensorId + "' was not found.", 404);
                    return new NotFoundException(Response.status(Response.Status.NOT_FOUND).entity(err).build());
                });

        // Business Logic: Block readings if sensor is MAINTENANCE or OFFLINE.
        // A sensor that is not physically active cannot accept new measurements.
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently under MAINTENANCE and cannot accept new readings. "
                    + "Update the sensor status to ACTIVE before posting readings.");
        }
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is OFFLINE and cannot accept new readings. "
                    + "Bring the sensor back online before posting readings.");
        }

        // Validate that a value was supplied
        if (reading == null) {
            ErrorMessage error = new ErrorMessage("Request body is required with a 'value' field.", 400);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Auto-generate ID (UUID) and timestamp if not supplied by the client
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        repository.addReading(sensorId, reading);

        // Side effect: currentValue on the parent Sensor is updated inside DataRepository.addReading()
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
