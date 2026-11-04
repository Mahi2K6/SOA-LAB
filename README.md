# Smart Tourism Portal — Load Balancing & Service Discovery

Multiple instances of the **Tourist Service** run behind a **Spring Cloud API Gateway**.
Every instance registers with a **Eureka Server**; the gateway looks up `tourist-service`
in Eureka and spreads incoming requests across all live instances (round robin), so no
single instance becomes the bottleneck under heavy traffic.

```
                         ┌──────────────────────────┐
                         │   Eureka Server :8761    │  service registry
                         └──────────▲───────▲───────┘
            register + heartbeat    │       │  fetch instance list
       ┌────────────┬───────────────┘       │
       │            │            │          │
┌──────┴─────┐┌─────┴──────┐┌────┴───────┐  │
│ tourist    ││ tourist    ││ tourist    │  │
│ :8081      ││ :8082      ││ :8083      │  │
└──────▲─────┘└─────▲──────┘└────▲───────┘  │
       │   round robin (lb://)   │          │
       └────────────┬────────────┘          │
             ┌──────┴──────────────────────┴┐
             │     API Gateway :8080        │  single entry point
             └──────────────▲───────────────┘
                            │
                     Client / Browser
```

## Tech stack
- Java 17+, Spring Boot 3.3.5, Spring Cloud 2023.0.3
- Spring Cloud Netflix Eureka (server + client)
- Spring Cloud Gateway + Spring Cloud LoadBalancer (round robin)

## Modules

| Module | Port | Role |
|---|---|---|
| `eureka-server` | 8761 | Service registry (`@EnableEurekaServer`) |
| `tourist-service` | 8081, 8082, 8083 | Business service, run as 3 instances |
| `api-gateway` | 8080 | Routes `/api/tourist/**` to `lb://tourist-service` |

## How it works

1. **Service discovery** — each tourist-service instance starts with the same
   `spring.application.name: tourist-service` but a different port, and a unique
   `instance-id` (`tourist-service:8081`, …). They register with Eureka and send a
   heartbeat every 5 s.
2. **Load balancing** — the gateway route uses `uri: lb://tourist-service`. The `lb://`
   scheme makes Spring Cloud LoadBalancer fetch the instance list from Eureka and pick
   the next one round-robin for every request.
3. **Fault tolerance** — if an instance is stopped, Eureka evicts it within ~10 s and the
   gateway stops routing to it. Until then, the `Retry` filter retries a failed GET on the
   next instance, so the user does not see the error.
4. **Visibility** — every response contains `servedBy` and `port`, and the gateway adds an
   `X-Routed-To` header and logs `GET /api/tourist/... -> 172.x.x.x:8082`.

## Run it

```bash
mvn clean package -DskipTests
```

**Windows:** `start-all.bat` (opens one window per service)
**Linux/macOS:** `chmod +x *.sh && ./start-all.sh`

Or run manually, each in its own terminal, in this order:

```bash
java -jar eureka-server/target/eureka-server-1.0.0.jar
java -jar tourist-service/target/tourist-service-1.0.0.jar --server.port=8081
java -jar tourist-service/target/tourist-service-1.0.0.jar --server.port=8082
java -jar tourist-service/target/tourist-service-1.0.0.jar --server.port=8083
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

**From IntelliJ / STS / Eclipse:** run `TouristServiceApplication` once, then copy the run
configuration twice and add the program argument `--server.port=8082` and
`--server.port=8083` (IntelliJ: *Edit Configurations → Modify options → Allow multiple instances*).

Wait about 30 seconds, then open **http://localhost:8761** — under *Instances currently
registered* you should see `TOURIST-SERVICE` with 3 instances, plus `API-GATEWAY`.

## Run from Eclipse (one click)

1. Build: select `smart-tourism` → Run ▸ Run As ▸ Maven build (Goals can be left empty — the
   parent pom sets `<defaultGoal>clean package</defaultGoal>`).
2. Start everything: Run toolbar dropdown ▸ **Start All - Smart Tourism**. This launch group
   (in `launch/`) starts Eureka, waits 20 s, starts tourist-service on 8081/8082/8083, waits
   15 s, then starts the API gateway. Each one gets its own Console tab.
3. Stop: Console view ▸ red Terminate button on each, or "Remove All Terminated".

> The Eureka and Gateway modules use artifactIds `tourism-eureka-server` and
> `tourism-api-gateway` so they don't clash with other workspace projects that are also
> called `eureka-server` / `api-gateway`. Jar names are unchanged.

## Demonstrate load balancing

**Windows:** `.\test-load-balancing.ps1`  **Linux/macOS:** `./test-load-balancing.sh`

Expected output:

```
Request 1 -> tourist-service:8081
Request 2 -> tourist-service:8082
Request 3 -> tourist-service:8083
Request 4 -> tourist-service:8081
...
Distribution:
  tourist-service:8081 : 3 requests
  tourist-service:8082 : 3 requests
  tourist-service:8083 : 3 requests
```

(The starting instance may differ; the rotation is what matters.)

**Failover demo:** stop the 8082 window, run the test again — requests keep succeeding and
are split between 8081 and 8083. Start 8082 again and it rejoins the rotation within ~10 s,
with no change to the gateway.

## API (always call through the gateway, port 8080)

| Method | URL | Description |
|---|---|---|
| GET | `/api/tourist/instance` | Which instance served the request |
| GET | `/api/tourist/places` | All tourist places |
| GET | `/api/tourist/places?city=Vijayawada` | Places in a city |
| GET | `/api/tourist/places/{id}` | One place |
| POST | `/api/tourist/places` | Add a place (JSON body) |
| PUT | `/api/tourist/places/{id}` | Update a place |
| DELETE | `/api/tourist/places/{id}` | Delete a place |
| GET | `/gateway/services` | Services the gateway sees in Eureka |
| GET | `/gateway/services/tourist-service` | Live tourist-service instances |
| GET | `/actuator/gateway/routes` | Gateway route definitions |

Sample POST body:

```json
{ "name": "Bhavani Island", "city": "Vijayawada", "category": "Nature",
  "description": "River island on the Krishna", "rating": 4.2 }
```

> Data is in memory per instance, so a place added through one instance is not visible
> on the others. That is deliberate to keep the demo setup-free; in production all
> instances would share one database (MySQL/PostgreSQL), which is what makes stateless
> horizontal scaling work.

## Key configuration

`api-gateway/src/main/resources/application.yml`
```yaml
spring.cloud.gateway.routes:
  - id: tourist-service
    uri: lb://tourist-service        # discover via Eureka + load balance
    predicates: [ Path=/api/tourist/** ]
    filters: [ Retry ... ]           # try the next instance on failure
```

`tourist-service/src/main/resources/application.yml`
```yaml
spring.application.name: tourist-service          # same name for every instance
eureka.instance.instance-id: ${spring.application.name}:${server.port}  # unique per instance
```

## Viva points
- **Why a gateway?** One entry point, hides instance addresses, central place for routing,
  retries, auth and rate limiting.
- **Why Eureka?** Instances come and go and their addresses change; the registry gives the
  gateway the current list instead of hard-coded URLs.
- **Client-side vs server-side LB:** here the gateway itself (a client of Eureka) picks the
  instance — client-side load balancing. Nginx/AWS ELB would be server-side.
- **Round robin** is the default; Spring Cloud LoadBalancer also ships a `RandomLoadBalancer`.
- **Scaling out** = start another instance on a new port. No config change anywhere else.
