package org.example.iws_websitesneaker.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FlexibleDateDeserializer extends JsonDeserializer<Date> {

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy"
    };

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String dateString = jsonParser.getText();

        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                if (format.contains("Z") || format.contains("XXX")) {
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                } else {
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                }
                return sdf.parse(dateString);
            } catch (ParseException e) {
                // Try next format
            }
        }

        throw new RuntimeException("Unable to parse date: " + dateString +
                ". Supported formats: yyyy-MM-dd HH:mm:ss, yyyy-MM-dd'T'HH:mm:ss.SSS'Z', etc.");
    }
}


