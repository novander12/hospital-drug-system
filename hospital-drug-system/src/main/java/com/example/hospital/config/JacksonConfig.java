package com.example.hospital.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register the Hibernate5 module
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        // Optional: Configure features if needed, e.g., serialize lazy-loaded objects as null
        // hibernate5Module.configure(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        mapper.registerModule(hibernate5Module);

        // Register the JavaTimeModule for Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());

        // Optional: Configure JavaTimeModule to write dates as ISO-8601 strings
        // instead of timestamps (numeric array)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
} 