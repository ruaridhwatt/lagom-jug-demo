package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nimbusds.jose.jwk.JWKSet;
import net.minidev.json.JSONObject;

import java.io.IOException;

public class JWKSetSerializer extends JsonSerializer<JWKSet> {

    @Override
    public void serialize(JWKSet jwkSet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        JsonSerializer<Object> valueSerializer = serializerProvider.findValueSerializer(JSONObject.class);
        valueSerializer.serialize(jwkSet.toJSONObject(), jsonGenerator, serializerProvider);
    }
}
