package com.foo.durian.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

/**
 * Version 1.0.0
 * Created by f on 16/11/21.
 */
public class DateTimeDeserializer extends JsonDeserializer<DateTime> {

    /*
      注意: 此处的日期格式可能需要扩展
     */
    @Override
    public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String dateTimeStr = jp.getValueAsString();
        return DateTime.parse(dateTimeStr, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
