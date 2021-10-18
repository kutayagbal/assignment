# Assignment Project
Implementation of an API aggregator backend project. Application queues outgoing requests for same API. After the queue size reaches a certain number or the oldest item in the queue reaches a certain duration, application creates one aggregated outgoing request and returns corresponding responses to seperate clients.

## Steps to run
1. Build the project using
   `mvn clean install`
2. Run using `mvn spring-boot:run`
3. The web application is accessible via localhost:8080

Techstack:
-Java11
-Spring Boot
-REST
-Async
-CompletableFuture
-TimerTask
-ConcurrentLinkedQueue
-RestTemplate

Unit/Integration Test:
-JUnit5
-Mockito
-Suite
-MockBean
-SpyBean
