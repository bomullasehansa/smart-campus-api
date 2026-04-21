package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.ErrorMessage;
import com.smartcampus.api.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.repository.DataRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * JAX-RS Resource for managing campus Sensors.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final DataRepository repository = DataRepository.getInstance();

    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        Collection<Sensor> allSensors = repository.getAllSensors();
        if (type != null && !type.isEmpty()) {
            return allSensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return allSensors;
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            ErrorMessage error = new ErrorMessage("Sensor 'id' is required.", 400);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (repository.getSensor(sensor.getId()).isPresent()) {
            ErrorMessage error = new ErrorMessage("Sensor with ID '" + sensor.getId() + "' already exists.", 409);
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }
        if (!repository.getRoom(sensor.getRoomId()).isPresent()) {
            throw new LinkedResourceNotFoundException("Room with ID '" + sensor.getRoomId() + "' not found.");
        }
        repository.addSensor(sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        return repository.getSensor(sensorId)
                .map(sensor -> Response.ok(sensor).build())
                .orElseGet(() -> {
                    ErrorMessage error = new ErrorMessage("Sensor not found.", 404);
                    return Response.status(Response.Status.NOT_FOUND).entity(error).build();
                });
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!repository.getSensor(sensorId).isPresent()) {
            throw new NotFoundException("Sensor not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
