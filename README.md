# Smart Campus API

## Overview

This is a robust, scalable RESTful API built for the University's **"Smart Campus"** initiative.  
It manages campus **Rooms**, **Sensors** (CO2, Temperature, Occupancy, etc.), and historical **Sensor Readings**, providing a seamless interface for facilities managers and automated building systems.

**Technology Stack:**

| Technology     | Detail                              |
|----------------|-------------------------------------|
| Language       | Java 11                             |
| Framework      | JAX-RS (GlassFish Jersey 2.39)      |
| Embedded Server| Grizzly HTTP Container              |
| Build Tool     | Maven 3.x                           |
| Data Storage   | Thread-safe In-memory ConcurrentHashMap (no database) |
| Testing Tools  | Postman Collection + PowerShell scripts included |

---

## Build and Run Instructions

### Prerequisites
- **JDK 11** or higher (`java -version` to verify)
- **Maven 3.x** (`mvn -version` to verify)

### Steps

1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
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

## Submission Materials

### 1. Postman Collection
A pre-configured Postman collection is included for your video demonstration:  
📁 `postman/SmartCampusAPI.postman_collection.json`

Import it into Postman to instantly test all endpoints.

### 2. Demo Data Script
To populate the API with sample data for your presentation, run:
```powershell
./scripts/populate_data.ps1
```

### 3. Conceptual Report
Theory answers are in this README (below) and also in [REPORT.md](./REPORT.md) for PDF conversion.

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

---

## Conceptual Report (Theory Answers)

### Part 1: Service Architecture

**Q1: Explain the default lifecycle of a JAX-RS Resource class and the synchronization impact.**

By default, JAX-RS Resource classes are **request-scoped** — a fresh instance is created for every incoming HTTP request and discarded after the response is sent. This promotes thread isolation but means resource classes cannot safely store persistent state in instance variables.

For the Smart Campus API, which requires persistent in-memory data, this means all shared state must live in an external **Singleton** (`DataRepository`). We use `ConcurrentHashMap` and `Collections.synchronizedList()` to prevent race conditions when multiple clients interact with the API simultaneously.

---

**Q2: Why is HATEOAS considered a hallmark of advanced RESTful design?**

HATEOAS (Hypermedia as the Engine of Application State) means the server embeds navigational links in every response. This decoupling allows the server to change URL structures without breaking clients, as they follow links rather than hard-coding paths.
