package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.ErrorMessage;
import com.smartcampus.api.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.repository.DataRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * JAX-RS Resource for managing campus Sensors.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final DataRepository repository = DataRepository.getInstance();

    @GET
    public Collection<Sensor> getSensors() {
        return repository.getAllSensors();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            ErrorMessage error = new ErrorMessage("Sensor 'id' is required.", 400);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (!repository.getRoom(sensor.getRoomId()).isPresent()) {
            throw new LinkedResourceNotFoundException("Room with ID '" + sensor.getRoomId() + "' not found.");
        }
        repository.addSensor(sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }
}
