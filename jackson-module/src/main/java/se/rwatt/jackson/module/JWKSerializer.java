package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nimbusds.jose.jwk.JWK;
import net.minidev.json.JSONObject;

import java.io.IOException;

public class JWKSerializer extends JsonSerializer<JWK> {
    @Override
    public void serialize(JWK jwk, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws
            IOException {
        JsonSerializer<Object> valueSerializer = serializerProvider.findValueSerializer(JSONObject.class);
        valueSerializer.serialize(jwk.toJSONObject(), jsonGenerator, serializerProvider);
    }
}
