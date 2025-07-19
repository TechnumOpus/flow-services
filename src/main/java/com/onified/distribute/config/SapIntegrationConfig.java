package com.onified.distribute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SapIntegrationConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2XmlHttpMessageConverter());
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    @Bean
    @Primary  // This makes it the default ObjectMapper for the entire application
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register JSR310 module for Java 8 time support
        objectMapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps (write as ISO-8601 strings instead)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }

    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();

        // Register JSR310 module for XML mapper too
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return xmlMapper;
    }

    @Bean
    public Map<String, String> productLocationMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("THT1 - Raw Material WH", "TYG012CWBC4");
        mapping.put("THT1 - Packing Material WH", "301013467");
        mapping.put("THT1 - Greigh Folding WH", "TGBSPTDPDNSUSGNZ06928");
        mapping.put("THT1 - FG WH", "TFBSPTDPDNSUSGNZ06928F0901375");
        return mapping;
    }
}