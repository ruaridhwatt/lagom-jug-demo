package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class MinidevJSONObjectSerializer extends JsonSerializer<JSONObject> {
    @Override
    public void serialize(JSONObject jsonObject, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        SortedSet<String> keys = new TreeSet<>(jsonObject.keySet());
        jsonGenerator.writeStartObject();
        for (String k : keys) {
            jsonGenerator.writeFieldName(k);
            Object value = jsonObject.get(k);
            JsonSerializer<Object> serializer = serializerProvider.findValueSerializer(value.getClass());
            serializer.serialize(value, jsonGenerator, serializerProvider);
        }
        jsonGenerator.writeEndObject();
    }
}
