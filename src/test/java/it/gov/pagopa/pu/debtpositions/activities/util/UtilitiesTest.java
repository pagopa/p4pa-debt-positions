package it.gov.pagopa.pu.debtpositions.activities.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UtilitiesTest {

    @Test
    void testIbanInvalid(){
        String iban = "test";
        boolean result = Utilities.isValidIban(iban);
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345", "12345abc123", "1234/abc123"})
    void testValidateEmptyPIVA(String piva){
        boolean result = Utilities.isValidPIVA(piva);
        assertFalse(result);
    }


}
