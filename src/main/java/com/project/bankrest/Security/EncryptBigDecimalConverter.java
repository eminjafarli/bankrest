package com.project.bankrest.Security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Converter
@RequiredArgsConstructor
public class EncryptBigDecimalConverter implements AttributeConverter<BigDecimal, String> {

    private final EncryptionUtil encryptionUtil;

    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        return attribute == null ? null : encryptionUtil.encrypt(attribute.toPlainString());
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new BigDecimal(encryptionUtil.decrypt(dbData));
    }
}
