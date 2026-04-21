## Part 1: Service Architecture & Setup

### Q1: Explain the default lifecycle of a JAX-RS Resource class.
In the default configuration of JAX-RS, Resource classes are **Request-scoped**. This means that the JAX-RS runtime creates a fresh instance of the resource class for every single incoming HTTP request. Once the request has been processed and the response is sent back to the client, the instance is discarded and made available for garbage collection.

**Synchronization Impact:** This architectural decision implies that resource classes cannot safely store state in instance variables unless that state is meant only for a single request. For our "Smart Campus" API, which requires persistent in-memory data (rooms and sensors), we must use a **Singleton** pattern for our data repository and utilize thread-safe data structures like `ConcurrentHashMap`. This ensures that multiple concurrent requests from different clients do not cause race conditions or data inconsistency when modifying the campus state.

### Q2: Why is the provision of ”Hypermedia” (HATEOAS) considered a hallmark of advanced RESTful design?
HATEOAS (Hypermedia as the Engine of Application State) is the final level of the Richardson Maturity Model for REST APIs. It is considered a hallmark of advanced design because it decouples the client from the server's URL structure. By providing links (hypermedia) within the response body, the server tells the client what actions are currently possible and where to go to perform them.

**Benefit over static docs:** Unlike static documentation (which can become outdated), HATEOAS makes the API self-documenting in real-time. If the URL for "rooms" changes from `/api/v1/rooms` to `/api/v1/campus/rooms`, a HATEOAS-compliant client will naturally follow the new link provided in the Discovery endpoint without requiring a code change or a manual update to the client's configuration.

---

## Part 2: Room Management

### Q3: When returning a list of rooms, what are the implications of returning only IDs versus full room objects?
- **Returning only IDs:** This approach minimizes network bandwidth and reduces the size of the initial response payload. It is very efficient for large collections. However, it forces the client to make "N+1" requests (one for the list, and one for each specific room's details), which can lead to high latency and excessive server load if the client needs to display full room details in a dashboard.
- **Returning full objects:** This provides the client with all necessary information in a single round-trip, improving the user experience for detail-heavy views. The downside is increased network usage and slower processing time for the server and client as the list grows into the thousands.

### Q4: Is the DELETE operation idempotent in your implementation?
Yes, the DELETE operation is idempotent. Idempotency means that making the same request multiple times will have the same effect on the server state as making it once.
- On the **first call** to `DELETE /rooms/{id}`, the room is removed from the system, and the server returns a `204 No Content`.
- On **subsequent calls** to exactly the same URL, the room no longer exists. The server will return a `404 Not Found`.
Crucially, the *state of the server* (the absence of the room) remains unchanged after the second, third, or hundredth call.

---

## Part 3: Sensor Operations & Linking

### Q5: Explain the technical consequences if a client attempts to send data in a different format (e.g., text/plain).
Because we have explicitly used the `@Consumes(MediaType.APPLICATION_JSON)` annotation on our resource methods, the JAX-RS runtime performs automatic content negotiation. If a client sends a request with a `Content-Type: text/plain` header, the JAX-RS server will intercept this mismatch and automatically return an **HTTP 415 Unsupported Media Type** response. The business logic inside the resource method will never be executed, protecting the API from malformed or unexpected data formats.

### Q6: Contrast Query Parameters vs Path Parameters for filtering.
- **Path Parameters (e.g., `/sensors/type/CO2`):** These are traditionally used to identify a specific resource or a sub-resource. Using them for filtering makes the URL structure rigid and implies a hierarchical relationship that might not exist.
- **Query Parameters (e.g., `/sensors?type=CO2`):** These are semantically designed for modifiers like filtering, searching, and sorting. They are superior because they are optional and can be combined (e.g., `?type=CO2&status=ACTIVE`) without creating a complex explosion of URL paths. It keeps the primary resource endpoint (`/sensors`) consistent.

---

## Part 4: Deep Nesting with Sub-Resources

### Q7: Discuss the architectural benefits of the Sub-Resource Locator pattern.
The Sub-Resource Locator pattern (e.g., delegating `sensors/{id}/readings` to a `SensorReadingResource` class) promotes the **Single Responsibility Principle**. It allows the developer to break down a large, complex API into small, manageable controller classes.
Instead of having one "Giant Resource" class that handles hundreds of endpoints, each sub-resource class can focus purely on its own logic (like historical reading management). This makes the code significantly easier to maintain, read, and unit-test.

---

## Part 5: Advanced Error Handling & Logging

### Q8: Why is HTTP 422 often more semantically accurate than a standard 404 for missing references?
A **404 Not Found** status typically indicates that the URI itself (the "endpoint") does not exist on the server. In contrast, an **HTTP 422 Unprocessable Entity** indicates that the server understands the content-type of the request and the syntax is correct, but the *semantic content* contains logical errors. 
In our case, the client is trying to link a sensor to a `roomId`. If that ID doesn't exist, the *request payload* is invalid, not the URI. Using 422 tells the client specifically that their data reference is invalid.

### Q9: Explain the risks associated with exposing internal Java stack traces.
Exposing stack traces to external API consumers is a major security vulnerability (Information Leakage). A stack trace reveals:
1. The exact versions of frameworks and libraries being used (e.g., Jersey versions).
2. The internal package structure and class names of the application.
3. Database driver details and sometimes even SQL fragments.
An attacker can use this information to identify known vulnerabilities in those specific versions or to craft a targeted multi-stage attack like SQL Injection or Remote Code Execution (RCE).

### Q10: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging?
Using filters implements the **Don't Repeat Yourself (DRY)** principle. Logging is a "cross-cutting concern" because it applies to almost every endpoint. Inserting manual log statements into every resource method leads to "boilerplate bloat" and is prone to human error (forgetting a method). 
A `ContainerRequestFilter` and `ContainerResponseFilter` intercept every single call in a centralized place, ensuring that logging is **consistent, global, and decoupled** from the business logic.
