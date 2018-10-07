package com.foo.durian.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 显示正号的数字
 *
 * Created by f on 16/12/22.
 */
@SuppressWarnings("all")
public class BigDecimalMarkedJsonSerializer extends JsonSerializer<BigDecimal> {
    private static DecimalFormat decimalFormat = new DecimalFormat("0.##");

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        decimalFormat.setPositivePrefix("+");
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(decimalFormat.format(value));
        }
    }
}
