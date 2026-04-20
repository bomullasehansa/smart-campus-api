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

    // ─── Sensor Operations ──────────────────────────────────────────────────

    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    public Optional<Sensor> getSensor(String id) {
        return Optional.ofNullable(sensors.get(id));
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        Room room = rooms.get(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
    }

    public void removeSensor(String sensorId) {
        Sensor sensor = sensors.remove(sensorId);
        if (sensor != null) {
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().remove(sensorId);
            }
            readings.remove(sensorId);
        }
    }

    // ─── Sensor Reading Operations ──────────────────────────────────────────

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.getOrDefault(sensorId, Collections.emptyList());
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(reading);

        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }
}
