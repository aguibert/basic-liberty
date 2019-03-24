Simple integration testing of JavaEE and MicroProfile apps using Testcontainers:

To run integration tests:
```
./gradlew test
```

To run the app as a docker container locally and tail logs:
```
./gradlew composeUp
```

To stop all containers:
```
./gradlew composeDown
```
