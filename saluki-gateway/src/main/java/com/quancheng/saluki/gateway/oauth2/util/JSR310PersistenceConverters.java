package com.quancheng.saluki.gateway.oauth2.util;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public final class JSR310PersistenceConverters {

    private JSR310PersistenceConverters() {
    }

    @Converter(autoApply = true)
    public static class LocalDateConverter implements AttributeConverter<LocalDate, java.sql.Date> {

        @Override
        public java.sql.Date convertToDatabaseColumn(LocalDate date) {
            return date == null ? null : java.sql.Date.valueOf(date);
        }

        @Override
        public LocalDate convertToEntityAttribute(java.sql.Date date) {
            return date == null ? null : date.toLocalDate();
        }
    }

    @Converter(autoApply = true)
    public static class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Date> {

        @Override
        public Date convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
            return JSR310DateConverters.ZonedDateTimeToDateConverter.INSTANCE.convert(zonedDateTime);
        }

        @Override
        public ZonedDateTime convertToEntityAttribute(Date date) {
            return JSR310DateConverters.DateToZonedDateTimeConverter.INSTANCE.convert(date);
        }
    }

    @Converter(autoApply = true)
    public static class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

        @Override
        public Date convertToDatabaseColumn(LocalDateTime localDateTime) {
            return JSR310DateConverters.LocalDateTimeToDateConverter.INSTANCE.convert(localDateTime);
        }

        @Override
        public LocalDateTime convertToEntityAttribute(Date date) {
            return JSR310DateConverters.DateToLocalDateTimeConverter.INSTANCE.convert(date);
        }
    }
}
