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

## Part 1: Service Architecture & Setup

### 1. Project & Application Configuration
**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

In JAX-RS, resource classes follow a request-scoped lifecycle by default. The runtime creates a brand-new instance of each resource class for every incoming HTTP request, processes the request, and then discards the instance to be garbage collected once the response has been sent. This means no state is retained between requests inside the resource class itself.

This design has a critical implication for the Smart Campus API: any data stored as an instance variable in a resource class would be lost after each request. Since the system requires persistent in-memory storage of rooms, sensors, and readings across many requests, a Singleton repository pattern is required. The DataRepository class is implemented as a thread-safe Singleton using double-checked locking, and it stores all data in ConcurrentHashMap instances rather than plain HashMaps.

The use of ConcurrentHashMap is essential because multiple clients may send requests simultaneously. Unlike a standard HashMap, ConcurrentHashMap handles concurrent reads and writes without data corruption or race conditions ensuring that two clients cannot simultaneously corrupt the room or sensor data.

---

### 2. The "Discovery" Endpoint
**Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS (Hypermedia as the Engine of Application State) is the most mature according to the Richardson Maturity Model for REST APIs. Its fundamental idea is that, instead of depending on pre-programmed knowledge of URL structures, a client should be able to browse the full API by following hyperlinks included in server responses.

This separates the internal URL design of the server from the client. A HATEOAS-compliant client immediately follows the new link from the Discovery response whenever the server modifies an endpoint path, such as changing rooms from /api/v1/rooms to /api/v1/campus/rooms. It does not need a code change or a documentation update.

In contrast, static documentation becomes outdated the moment a URL changes. HATEOAS makes the API self-documenting in real-time, reduces coupling between components, and dramatically improves the long-term maintainability of both the server and its clients.

---

## Part 2: Room Management

### 3. Room Resource Implementation
**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

Returning only IDs minimises network bandwidth and produces a very small response payload. This is efficient when a client only needs to know which rooms exist. However, it creates an N+1 request problem: if the client needs to display full details for all rooms, it must make one request to get the list of IDs and then a separate GET request for each individual room. For a campus with hundreds of rooms, this generates excessive server load and high latency.

Returning full objects gives the client everything it needs in a single round-trip, improving user experience for detail-heavy dashboards. The trade-off is a larger response payload and slower serialisation as the number of rooms grows into the thousands. The optimal approach depends on usage context. For a list view, full objects are preferable. For large collections where the client only needs to pick one item, IDs with pagination would be more efficient.

---

### 4. Room Deletion & Safety Logic
**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, the DELETE operation is idempotent. Idempotency means that executing the same request multiple times produces the same server state as executing it once.

First call - DELETE /api/v1/rooms/{roomId}: If the room exists and has no sensors, it is removed from the repository and the server responds with 204 No Content. The room no longer exists in the system.

If the room has sensors assigned, the DELETE returns 409 Conflict and the room is not deleted. Repeating the same call will keep returning 409 as long as sensors remain - the server state is unchanged across all repeated calls, so this is also idempotent.

Subsequent calls on a deleted room - same URL: The room no longer exists. The server returns 404 Not Found. The state of the server - the absence of that room - has not changed. The system is in exactly the same state as after the first successful call.

The 404 on subsequent calls is expected and correct behaviour, not a violation of idempotency. The key point is that the server state remains unchanged, even though the response code differs.

---

## Part 3: Sensor Operations & Linking

### 5. Sensor Resource & Integrity
**Question:** We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The @Consumes(MediaType.APPLICATION_JSON) annotation instructs the JAX-RS runtime to perform automatic content negotiation before the request even reaches the business logic. When a request arrives, Jersey inspects the Content-Type header and compares it against the declared acceptable types.

If a client sends a request with Content-Type: text/plain, the JAX-RS runtime immediately rejects it and returns an HTTP 415 Unsupported Media Type response. The resource method body is never executed - the rejection happens at the framework layer. This protects the API from unparseable or malformed payloads and ensures that all data entering the system is valid JSON before any processing occurs.

---

### 6. Filtered Retrieval & Search
**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Semantically, path parameters (such as /sensors/type/CO2) are intended to identify a particular, distinct resource within the URL hierarchy. It is architecturally deceptive and produces inflexible URL structures to use a path parameter for filtering since it suggests that "CO2 sensors" is a fixed sub-resource of "sensors."

Filtering, searching, sorting, and pagination are examples of optional modifiers that are specifically created for query parameters (e.g., /sensors?type=CO2). They are better for three reasons:

1. They are optional - without a filter, GET /sensors still functions.
2. They can be coupled with other filters without causing URL path explosions, such as ?type=CO2&status=ACTIVE.
3. They maintain consistency in the primary resource endpoint - regardless of filtering, the canonical URL for "all sensors" is still /api/v1/sensors.

---

## Part 4: Deep Nesting with Sub-Resources

### 7. The Sub-Resource Locator Pattern
**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

A specialized class is given control over a nested URL path using the Sub-Resource Locator pattern. The SensorResource class in this implementation manages /api/v1/sensors; upon receiving a request for /sensors/{sensorId}/readings, it creates and returns a SensorReadingResource object that alone manages the readings logic. Each class has a single, distinct job, which supports the Single Responsibility Principle.

The main advantages of this architecture are:

1. Separation of concerns - Reading history logic and sensor lifetime logic are completely distinct, which facilitates reasoning about both.
2. Less complexity - Managing all nested routes with a single "god class" would soon grow to hundreds of lines and be challenging to manage.
3. Simpler unit testing - Without requiring the complete resource hierarchy to be set up, each class may be tested separately.
4. Scalability - It is possible to add additional sub-resources (such as sensor alerts or calibration history) as new classes without changing the ones that already exist.

---

## Part 5: Advanced Error Handling, Exception Mapping & Logging

### 8. Dependency Validation (422 Unprocessable Entity)
**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

An HTTP 404 Not Found error indicates that the endpoint itself cannot be located; the requested URI does not exist on the server. When a client explicitly requests /api/v1/rooms/FAKE-999, this is the appropriate response since the URL path resolves to nothing.

On the other hand, the URI itself is entirely fine when a client performs a legitimate POST request to /api/v1/sensors with a body that contains an invalid roomId. The right endpoint was located and executed by the server. The payload's semantic content references a linked resource that does not exist, which is the issue.

In this scenario, HTTP 422 Unprocessable Entity is semantically correct: the server understood the content-type, parsed the JSON successfully, but found a logical validation error within the data itself. It tells the client precisely: "Your request structure is fine, but your data references something that does not exist." This is actionable feedback that guides the client to fix the payload rather than the URL.

---

### 9. The Global Safety Net (500)
**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Stack trace exposure to API users is categorized as an Information Leakage issue (OWASP Top 10 - A05: Security Misconfiguration). An unprocessed stack trace shows many types of sensitive data:

1. Library and framework versions, such as precise Jersey or Grizzly version numbers. To find known, exploitable vulnerabilities in those particular versions, an attacker can cross-reference these with publicly available CVE databases.
2. Internal package and class names - the application's whole class path structure reveals its design and facilitates the creation of targeted exploit payloads by an attacker.
3. Business logic hints - stack traces might occasionally provide information about database drivers, SQL fragments, or internal method names that can be utilized to deduce how the program handles data.

The correct approach - implemented in the GlobalExceptionMapper - is to log the full stack trace server-side only via java.util.logging, while returning a safe, generic error message with no implementation details to the client.

---

### 10. API Request & Response Logging Filters
**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

The Don't Repeat Yourself (DRY) principle is broken and repetitive code scattered across every method results when Logger.info() calls are placed inside each resource method. It is also vulnerable to human error - a developer adding a new endpoint might forget to include the log statements, creating gaps in observability.

A ContainerRequestFilter and ContainerResponseFilter intercept every incoming request and outgoing response at the framework level, in one central place. This provides:

1. Consistency - every endpoint is logged uniformly, with no exceptions.
2. Separation of concerns - resource methods only contain business logic; observability is handled entirely elsewhere.
3. Maintainability - changing the log format requires editing one class, not every resource method across the entire codebase.
4. No coupling - resource classes have no dependency on the logging mechanism, making them easier to test in isolation.
