package com.foo.durian.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 *
 *
 * Created by foolish on 2016/11/17.
 */
public class DateTimeDaySerializer extends JsonSerializer<DateTime> {
    @Override
    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (value == null) {
            jgen.writeNull();
            return;
        }
        jgen.writeString(value.toString("yyyy-MM-dd"));
    }
}
