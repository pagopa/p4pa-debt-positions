package it.gov.pagopa.pu.debtpositions.citizen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.debtpositions.citizen.util.AESUtils;
import it.gov.pagopa.pu.debtpositions.citizen.util.HashAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class DataCipherService {

    private final String encryptPsw;
    private final HashAlgorithm hashAlgorithm;
    private final ObjectMapper objectMapper;

    public DataCipherService(
            @Value("${data-cipher.encrypt-psw}") String encryptPsw,
            @Value("${data-cipher.hash-pepper}") String hashPepper,
            ObjectMapper objectMapper
    ) {
        this.encryptPsw = encryptPsw;
        this.objectMapper = objectMapper;

        hashAlgorithm = new HashAlgorithm("SHA-256", Base64.getDecoder().decode(hashPepper));
    }

    public byte[] encrypt(String plainText) {
        return AESUtils.encrypt(encryptPsw, plainText);
    }

    public String decrypt(byte[] cipherData) {
        return AESUtils.decrypt(encryptPsw, cipherData);
    }

    public <T> byte[] encryptObj(T obj) {
        try {
            return encrypt(objectMapper.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize object as JSON", e);
        }
    }

    public <T> T decryptObj(byte[] cipherData, Class<T> clazz) {
        try {
            return objectMapper.readValue(decrypt(cipherData), clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot deserialize object as JSON", e);
        }
    }

    public byte[] hash(String value) {
        if (value == null) {
            return null;
        }
        return hashAlgorithm.apply(value.toUpperCase());
    }
}
