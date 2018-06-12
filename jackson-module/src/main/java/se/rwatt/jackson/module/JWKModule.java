package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class JWKModule extends SimpleModule {
    private static final String NAME = "JWKModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {};

    public JWKModule() {
        super(NAME, VERSION_UTIL.version());
        addDeserializer(JWK.class, new JWKDeserializer());
        addSerializer(JWK.class, new JWKSerializer());
        addDeserializer(JWKSet.class, new JWKSetDeserializer());
        addSerializer(JWKSet.class, new JWKSetSerializer());
        addDeserializer(JSONObject.class, new MinidevJSONObjectDeserializer());
        addSerializer(JSONObject.class, new MinidevJSONObjectSerializer());
    }
}
