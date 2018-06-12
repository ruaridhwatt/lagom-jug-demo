package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nimbusds.jose.jwk.JWK;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

public class JWKDeserializer extends JsonDeserializer<JWK> {
    @Override
    public JWK deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JSONObject obj = ctx.readValue(jp, JSONObject.class);
        try {
            return JWK.parse(obj);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
