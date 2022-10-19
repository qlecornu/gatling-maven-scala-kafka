Gatling plugin for Maven - Scala demo project with Kafka
=============================================

A simple showcase of a Maven project using the Gatling plugin for Maven and the Kafka Plugin from [Tinkoff](https://github.com/Tinkoff/gatling-kafka-plugin). Refer to the plugin documentation
[on the Gatling website](https://gatling.io/docs/current/extensions/maven_plugin/) for usage.

It includes:

* [Maven Wrapper](https://maven.apache.org/wrapper/), so that you can immediately run Maven with `./mvnw` without having
  to install it on your computer
*  `pom.xml` with the dependencies for using kafka and plugin for generate avro schema
* 2 sample Simulations class, for demonstrating sufficient Gatling functionality for kafka load testing (Plaintext request and Avro request)
* docker-compose file for the creation of a full kafka broker locally

How can I use it ?:

//TODO