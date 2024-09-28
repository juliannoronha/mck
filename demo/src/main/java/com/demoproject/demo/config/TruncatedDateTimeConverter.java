package com.demoproject.demo.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Converter for truncating LocalDateTime to seconds precision when persisting to the database.
 * This helps maintain consistency in timestamp storage and retrieval.
 */
@Converter
public class TruncatedDateTimeConverter implements AttributeConverter<LocalDateTime, LocalDateTime> {

    /**
     * Converts a LocalDateTime entity attribute to a database column representation.
     * Truncates the timestamp to seconds precision if not null.
     *
     * @param attribute The LocalDateTime to be converted
     * @return The truncated LocalDateTime or null if the input is null
     */
    @Override
    public LocalDateTime convertToDatabaseColumn(LocalDateTime attribute) {
        // Truncate to seconds precision if not null, otherwise return null
        return attribute != null ? attribute.truncatedTo(ChronoUnit.SECONDS) : null;
    }

    /**
     * Converts a database column LocalDateTime back to the entity attribute.
     * No conversion is needed as the database already stores truncated values.
     *
     * @param dbData The LocalDateTime from the database
     * @return The same LocalDateTime without modification
     */
    @Override
    public LocalDateTime convertToEntityAttribute(LocalDateTime dbData) {
        return dbData;
    }

    // TODO: Consider adding logging for debugging purposes
    // TODO: Evaluate if millisecond precision is needed in future versions
}