# Embedding serde plugin for Kafka UI

This is pluggable embedding serde implementation for [kafka-ui](https://github.com/provectus/kafka-ui/) when an
embedding is stored as a byte array. It will be rendered as an array of floats.

For properties description and configuration example please see [docker-compose](docker-compose/setup-example.yaml).

## Building locally

```shell
mvn clean package
```
