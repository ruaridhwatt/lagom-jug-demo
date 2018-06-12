package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.minidev.json.JSONArray;

import java.io.IOException;

public class MinidevJSONArraySerializer extends JsonSerializer<JSONArray> {
    @Override
    public void serialize(JSONArray objects, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for (Object o : objects) {
            JsonSerializer<Object> serializer = serializerProvider.findValueSerializer(o.getClass());
            serializer.serialize(o, jsonGenerator, serializerProvider);
        }
        jsonGenerator.writeEndArray();
    }
}
