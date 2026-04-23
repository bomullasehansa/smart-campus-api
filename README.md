# Smart Campus API

## Overview

This is a robust, scalable RESTful API built for the University's **"Smart Campus"** initiative.  
It manages campus **Rooms**, **Sensors** (CO2, Temperature, Occupancy, etc.), and historical **Sensor Readings**, providing a seamless interface for facilities managers and automated building systems.

**Technology Stack:**

| Technology     | Detail                              |
|----------------|-------------------------------------|
| Language       | Java 21                             |
| Framework      | JAX-RS (GlassFish Jersey 2.39)      |
| Embedded Server| Grizzly HTTP Container              |
| Build Tool     | Maven 3.x                           |
| Data Storage   | Thread-safe In-memory ConcurrentHashMap (no database) |
| Testing Tools  | Postman Collection + PowerShell scripts included |

---

## Build and Run Instructions

### Prerequisites
- **JDK 21** or higher (`java -version` to verify)
- **Maven 3.x** (`mvn -version` to verify)

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/bomullasehansa/smart-campus-api.git
   cd smart-campus-api
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

3. **Run the server:**
   ```bash
   mvn exec:java
   ```
   The API will be available at: `http://localhost:8080/api/v1`

4. **Verify it's running** — open your browser or run:
   ```bash
   curl http://localhost:8080/api/v1
   ```

> **Note:** The server runs in the foreground. Press `CTRL+C` to stop it.

---


## API Endpoint Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery — API metadata & navigation links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (fails if sensors exist) |
| GET | `/api/v1/sensors` | List all sensors |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor by ID |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (updates sensor's currentValue) |

---

## Sample Interactions (curl)

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
```

### 3. Get All Sensors
```bash
curl -X GET http://localhost:8080/api/v1/sensors
```

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 5. Register a New Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### 6. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 23.5}'
```

### 7. Delete a Room
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

---

## Conceptual Report (Theory Answers)

