package it.gov.pagopa.pu.debtpositions.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidationUtils {
    private ValidationUtils(){}

    public static List<String> checkReadOnlyFields(Object oldObject, Object newObject, List<String> readOnlyFieldNames) {
        List<String> updatedFields = new ArrayList<>();
        List<Field> readOnlyFields = Arrays.stream(oldObject.getClass().getDeclaredFields()).filter(f-> readOnlyFieldNames.contains(f.getName())).toList();
        for (Field readOnlyField : readOnlyFields) {
            try {
                checkReadOnlyField(oldObject, newObject, readOnlyField, updatedFields);
            }catch(IllegalAccessException e){
                throw new IllegalStateException(e.getMessage());
            }
        }
        return updatedFields;
    }

    private static void checkReadOnlyField(Object oldObject, Object newObject, Field readOnlyField, List<String> updatedFields) throws IllegalAccessException {
        readOnlyField.setAccessible(true);
        Object newValue = readOnlyField.get(newObject);
        Object oldValue = readOnlyField.get(oldObject);
        if((oldValue == null && newValue != null)
                || (oldValue!=null && !oldValue.equals(newValue))){
            updatedFields.add(readOnlyField.getName());
        }
    }
}
