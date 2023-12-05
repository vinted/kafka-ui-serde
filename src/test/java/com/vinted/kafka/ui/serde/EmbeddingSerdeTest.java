package com.vinted.kafka.ui.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.PropertyResolver;
import com.provectus.kafka.ui.serde.api.Serde;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class EmbeddingSerdeTest {
    private final PropertyResolver resolverMock = mock(PropertyResolver.class);
    private final JsonMapper jsonMapper = new JsonMapper();
    private final EmbeddingSerde serde = new EmbeddingSerde();

    @BeforeEach
    void setup() {
        serde.configure(resolverMock, null, null);
    }

    @ParameterizedTest
    @EnumSource
    void canBeAppliedToAnyTopic(Serde.Target target) {
        assertTrue(serde.canDeserialize("test", target));
        assertTrue(serde.canSerialize("test", target));
    }

    @ParameterizedTest
    @EnumSource
    void doesNoProvideSchemaDescription(Serde.Target target) {
        assertTrue(serde.getSchema("test", target).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[1.2, 3.4, 5.6, 7.38]",
            "[1.0, 2.0, 0.3, 0.45]",
    })
    void serializeAndDeserializeWorksInPair(String jsonString) {
        var serializer = serde.serializer("test", Serde.Target.VALUE);
        var serializedBytes = serializer.serialize(jsonString);

        var deserializer = serde.deserializer("test", Serde.Target.VALUE);
        var deserializeResult = deserializer.deserialize(null, serializedBytes);

        assertEquals(DeserializeResult.Type.JSON, deserializeResult.getType());
        assertTrue(deserializeResult.getAdditionalProperties().isEmpty());
        assertJson(jsonString, deserializeResult.getResult());
    }

    private void assertJson(String expected, String actual) {
        try {
            assertEquals(jsonMapper.readTree(expected), jsonMapper.readTree(actual));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
