package com.foo.durian.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Version 1.0.0 Created by f on 17/3/1.
 */
public class NumericDateTimeJsonDeserializer extends DateTimeDeserializer {

    @Override
    public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String dateTimeStr = jp.getValueAsString();
        if (dateTimeStr.matches("^[0-9]+$")) {
            return new DateTime(Long.parseLong(dateTimeStr));
        }
        return super.deserialize(jp, ctxt);
    }
}
