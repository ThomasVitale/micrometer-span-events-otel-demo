# micrometer-span-events-otel-demo

Run the application with out-of-the-box Grafana LGTM stack via Testcontainers:

```shell
./gradlew bootTestRun
```

Call the demo endpoint:

```shell
http :8080
```

You can now visualize the generated metrics and traces.

Grafana is listening on port 3000. Check your container runtime to find the port to which is exposed to your localhost
and access Grafana from http://localhost:<port>. The credentials are `admin`/`admin`.

The application is automatically configured to export metrics and traces to the Grafana LGTM stack via OpenTelemetry.
In Grafana, you can query the traces from the "Explore" page, selecting the "Tempo" data source. You can also visualize metrics in "Explore > Metrics".

<img src="/screenshot.png" alt="Visualization of the traces generated for the Spring Boot application" align="left" /></a>