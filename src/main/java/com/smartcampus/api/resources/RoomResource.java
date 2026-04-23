package com.smartcampus.api.resources;

import com.smartcampus.api.exceptions.ErrorMessage;
import com.smartcampus.api.exceptions.RoomNotEmptyException;
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

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        return repository.getRoom(roomId)
                .map(room -> Response.ok(room).build())
                .orElseGet(() -> {
                    ErrorMessage error = new ErrorMessage("Room not found.", 404);
                    return Response.status(Response.Status.NOT_FOUND).entity(error).build();
                });
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = repository.getRoom(roomId)
                .orElseThrow(() -> new NotFoundException(Response.status(Response.Status.NOT_FOUND).build()));

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot decommission room with active sensors.");
        }

        repository.removeRoom(roomId);
        return Response.noContent().build();
    }

    @GET
    @Path("/crash")
    @Produces(MediaType.APPLICATION_JSON)
    public Response crash() {
        throw new RuntimeException("Deliberate crash for testing");
    }

}
