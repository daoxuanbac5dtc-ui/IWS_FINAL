package org.example.iws_websitesneaker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // ===== QUAN TRá»ŒNG: HIBERNATE MODULE Äá»‚ Xá»¬ LÃ LAZY LOADING =====
        Hibernate6Module hibernateModule = new Hibernate6Module();

        // Cáº¥u hÃ¬nh Hibernate module Ä‘á»ƒ trÃ¡nh LazyInitializationException
        hibernateModule.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
        hibernateModule.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);

        // KHÃ”NG FORCE lazy loading Ä‘á»ƒ trÃ¡nh N+1 queries
        hibernateModule.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);

        // ÄÄƒng kÃ½ hibernate module
        mapper.registerModule(hibernateModule);

        // Register JavaTimeModule for better date/time handling
        mapper.registerModule(new JavaTimeModule());

        // ===== SERIALIZATION FEATURES =====
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);

        // THÃŠM: Xá»­ lÃ½ lazy objects
        mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);

        // ===== DESERIALIZATION FEATURES =====
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);

        // ===== DATE FORMAT CONFIGURATION =====
        try {
            // Kiá»ƒm tra xem FlexibleDateFormat cÃ³ tá»“n táº¡i khÃ´ng
            Class<?> flexibleDateFormatClass = Class.forName("org.example.iws_websitesneaker.config.FlexibleDateFormat");
            Object flexibleDateFormat = flexibleDateFormatClass.getDeclaredConstructor().newInstance();
            mapper.setDateFormat((java.text.DateFormat) flexibleDateFormat);
        } catch (Exception e) {
            System.err.println("Could not set FlexibleDateFormat, using default: " + e.getMessage());
            // Fallback to simple date format
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        }

        System.out.println("âœ… ObjectMapper configured successfully with Hibernate-safe settings");
        return mapper;
    }

    /**
     * Táº¡o FlexibleDateFormat class náº¿u chÆ°a cÃ³
     */
    public static class FlexibleDateFormat extends SimpleDateFormat {
        private static final String[] DATE_PATTERNS = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss.SSS",
                "yyyy-MM-dd",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy"
        };

        public FlexibleDateFormat() {
            super("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public java.util.Date parse(String source) throws java.text.ParseException {
            for (String pattern : DATE_PATTERNS) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    sdf.setLenient(false);
                    return sdf.parse(source);
                } catch (java.text.ParseException e) {
                    // Try next pattern
                }
            }
            // If all patterns fail, try the default
            return super.parse(source);
        }
    }
}
