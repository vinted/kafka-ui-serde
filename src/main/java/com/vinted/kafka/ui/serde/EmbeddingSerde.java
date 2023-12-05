package com.vinted.kafka.ui.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.PropertyResolver;
import com.provectus.kafka.ui.serde.api.SchemaDescription;
import com.provectus.kafka.ui.serde.api.Serde;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Optional;

public class EmbeddingSerde implements Serde {
    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    @Override
    public void configure(PropertyResolver serdeProperties, PropertyResolver kafkaClusterProperties, PropertyResolver globalProperties) {
        serdeProperties.getProperty("BYTE_ORDER", String.class).ifPresent(byteOrder -> {
            if (byteOrder.equals("BIG_ENDIAN")) {
                this.byteOrder = ByteOrder.BIG_ENDIAN;
            } else if (byteOrder.equals("LITTLE_ENDIAN")) {
                this.byteOrder = ByteOrder.LITTLE_ENDIAN;
            } else {
                throw new RuntimeException("Invalid byte order: " + byteOrder);
            }
        });
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Byte array embedding serde rendering them as an array of floats");
    }

    @Override
    public Optional<SchemaDescription> getSchema(String topic, Target type) {
        return Optional.empty();
    }

    @Override
    public boolean canDeserialize(String topic, Target type) {
        return true;
    }

    @Override
    public boolean canSerialize(String topic, Target type) {
        return true;
    }

    @Override
    public Serializer serializer(String topic, Target type) {
        return inputString -> {
            try {
                var embeddings = JSON_MAPPER.readValue(inputString, float[].class);

                return floatArrayToByteArray(embeddings);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Serialization error", e);
            }
        };
    }

    @Override
    public Deserializer deserializer(String topic, Target type) {
        return (recordHeaders, bytes) -> {
            try {
                var embeddings = byteArrayToFloatArray(bytes);

                var embeddingsJson = JSON_MAPPER.writeValueAsString(embeddings);

                return new DeserializeResult(embeddingsJson, DeserializeResult.Type.JSON, Collections.emptyMap());
            } catch (IOException e) {
                throw new RuntimeException("Deserialization error", e);
            }
        };
    }

    private byte[] floatArrayToByteArray(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * values.length).order(byteOrder);
        for (float value : values) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    private float[] byteArrayToFloatArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(byteOrder);
        float[] values = new float[bytes.length / 4];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.getFloat();
        }
        return values;
    }
}
