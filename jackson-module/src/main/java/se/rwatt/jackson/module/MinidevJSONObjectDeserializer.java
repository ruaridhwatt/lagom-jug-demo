package se.rwatt.jackson.module;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;

public class MinidevJSONObjectDeserializer extends JsonDeserializer<JSONObject> {
    @Override
    public JSONObject deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        JSONObject obj = new JSONObject();
        if (!jp.hasCurrentToken()) {
            jp.nextToken();
        }
        if (!jp.getCurrentToken().equals(JsonToken.START_OBJECT)) {
            throw new JsonParseException(jp, "Start token not Start object");
        }
        while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {
            switch (jp.getCurrentToken()) {
                case FIELD_NAME:
                    String key = jp.getText();
                    jp.nextToken();
                    Object value = parseValue(jp, ctx);
                    obj.appendField(key, value);
                    break;
                default:
                    throw new JsonParseException(jp, "Value before key");
            }
        }
        return obj;
    }

    private Object parseValue(JsonParser jp, DeserializationContext ctx) throws IOException {
        switch (jp.getCurrentToken()) {
            case START_OBJECT:
                return deserialize(jp, ctx);
            case VALUE_EMBEDDED_OBJECT:
                return jp.getEmbeddedObject();
            case START_ARRAY:
                return parseArray(jp, ctx);
            case VALUE_TRUE:
            case VALUE_FALSE:
                return jp.getValueAsBoolean();
            case VALUE_STRING:
                return jp.getValueAsString();
            case VALUE_NUMBER_INT:
                return jp.getValueAsInt();
            case VALUE_NUMBER_FLOAT:
                return jp.getValueAsDouble();
            case VALUE_NULL:
                return null;
            default:
                throw new JsonParseException(jp, "invalid value: " + jp.getText());
        }
    }

    private Object parseArray(JsonParser jp, DeserializationContext ctx) throws IOException {
        JSONArray array = new JSONArray();
        while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
            array.add(parseValue(jp, ctx));
        }
        return array;
    }
}
