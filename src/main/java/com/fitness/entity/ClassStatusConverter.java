package com.fitness.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ClassStatusConverter implements AttributeConverter<Classes.Status, String> {

    @Override
    public String convertToDatabaseColumn(Classes.Status status) {
        return status == null ? null : status.name();
    }

    @Override
    public Classes.Status convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return Classes.Status.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Fallback for unexpected values
            return Classes.Status.ACTIVE;
        }
    }
}
