package com.smartcampus.api.repository;

import com.smartcampus.api.models.Room;
import com.smartcampus.api.models.Sensor;
import com.smartcampus.api.models.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, in-memory data repository using the Singleton pattern.
 */
public class DataRepository {

    private static volatile DataRepository instance;

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataRepository() {}

    public static DataRepository getInstance() {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository();
                }
            }
        }
        return instance;
    }

    // ─── Room Operations ────────────────────────────────────────────────────

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Optional<Room> getRoom(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public void removeRoom(String id) {
        Room room = rooms.remove(id);
        if (room != null) {
            for (String sensorId : room.getSensorIds()) {
                readings.remove(sensorId);
                sensors.remove(sensorId);
            }
        }
    }
}
