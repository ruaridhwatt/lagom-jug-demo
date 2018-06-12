package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nimbusds.jose.jwk.JWKSet;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

public class JWKSetDeserializer extends JsonDeserializer<JWKSet> {
    @Override
    public JWKSet deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JSONObject obj = ctx.readValue(jp, JSONObject.class);
        try {
            return JWKSet.parse(obj);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
