package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.util.List;

class ValidationUtilsTest {
    @Test
    void givenUpdatedFieldsWhenCheckReadOnlyFieldsThenUpdatedFieldList(){
        DebtPositionTypeOrg oldObject = new DebtPositionTypeOrg();
        oldObject.setDescription("description");
        oldObject.setCode("code");
        oldObject.setIban("iban");
        DebtPositionTypeOrg newObject = new DebtPositionTypeOrg();
        newObject.setDescription("newDescription");
        newObject.setCode("newCode");
        newObject.setIban("iban");
        List<String> expectedResult = List.of("code", "description");

        List<String> result = ValidationUtils.checkReadOnlyFields(oldObject, newObject, List.of("code", "description", "iban"));

        Assertions.assertFalse(CollectionUtils.isEmpty(result));
        Assertions.assertEquals(2,result.size());
        Assertions.assertEquals(expectedResult,result);
    }

    @Test
    void givenNoUpdatedFieldsWhenCheckReadOnlyFieldsThenEmptyList(){
        DebtPositionTypeOrg oldObject = new DebtPositionTypeOrg();
        oldObject.setDescription("description");
        oldObject.setCode("code");
        oldObject.setIban("iban");
        DebtPositionTypeOrg newObject = new DebtPositionTypeOrg();
        newObject.setDescription("description");
        newObject.setCode("code");
        newObject.setIban("iban");

        List<String> result = ValidationUtils.checkReadOnlyFields(oldObject, newObject, List.of("code", "description", "iban"));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(CollectionUtils.isEmpty(result));
    }
}
