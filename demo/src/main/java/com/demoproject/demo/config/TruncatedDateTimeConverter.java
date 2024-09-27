package com.demoproject.demo.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Converter
public class TruncatedDateTimeConverter implements AttributeConverter<LocalDateTime, LocalDateTime> {

    @Override
    public LocalDateTime convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute != null ? attribute.truncatedTo(ChronoUnit.SECONDS) : null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(LocalDateTime dbData) {
        return dbData;
    }
}