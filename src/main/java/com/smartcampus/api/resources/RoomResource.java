package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.ErrorMessage;
import com.smartcampus.api.models.Room;
import com.smartcampus.api.repository.DataRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * JAX-RS Resource for managing campus Rooms.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    private final DataRepository repository = DataRepository.getInstance();

    @GET
    public Collection<Room> getRooms() {
        return repository.getAllRooms();
    }

    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            ErrorMessage error = new ErrorMessage("Room 'id' field is required.", 400);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (repository.getRoom(room.getId()).isPresent()) {
            ErrorMessage error = new ErrorMessage("Room with ID already exists.", 409);
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }
        repository.addRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }
}
