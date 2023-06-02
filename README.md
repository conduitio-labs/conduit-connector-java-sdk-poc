# sdk

This a Java SDK for Conduit connector. This project uses [Quarkus](https://quarkus.io/).

## Creating a new connector
1. Create a new Maven project
2. Add this SDK as a dependency:
    ```xml
    <dependency>
        <groupId>io.conduit</groupId>
        <artifactId>conduit-connector-java-sdk-poc</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    ```
3. Create `src/main/resources/application.properties` with the following contents:
    ```properties
    quarkus.package.main-class=io.conduit.sdk.Main
    quarkus.grpc.server.port=<grpc port goes here>
    quarkus.http.port=<http port goes here>
    ```
4. Copy the build configuration from this SDK.
5. You need to provide a connector specification and a source/destination. That's done by implementing the
corresponding interfaces: 
  * `io.conduit.sdk.Specification`
  * `io.conduit.sdk.Source`
  * `io.conduit.sdk.Destination`

and then annotating them with `@jakarta.enterprise.context.ApplicationScoped`.

## Examples:
* Source connector: https://github.com/conduitio-labs/conduit-connector-generator-java
* Destination connector: https://github.com/conduitio-labs/conduit-connector-file-java

## Running the connector in dev mode (not tested)

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the connector

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/connector-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## ToDos

- [ ] make it possible to bootstrap a connector without so many manual steps (e.g. template repository, Maven parent project, Maven archetype) 
- [ ] reflection doesn't quite work
- [ ] update docs, which were copied from the Go SDK
- [ ] validations
- [ ] destination: batching
- [ ] destination.stop: wait for last position to be written
- [ ] logging