package it.gov.pagopa.pu.debtpositions.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.*;

@Slf4j
public class TestUtils {

  private TestUtils(){}

  /**
   * It will assert not null on all o's fields
   */
  public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field "+f.getName()+" of the input object of type "+o.getClass()+" is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()));
  }

  public static List<Method> reflectionEqualsByName(Object o1, Object o2, String... ignoredFields) {
    Set<String> ignoredFieldSet = new HashSet<>(Arrays.asList(ignoredFields));
    Assertions.assertFalse(o1 == null ^ o2 == null, String.format("Both objects have to be null or not null:%n%s%n%s", o1 == null ? "null" : o1.getClass().getName(), o2 == null ? "null" : o2.getClass().getName()));

    List<Method> checked = new ArrayList<>();

    if (o1 != null) {
      for (Method m1 : o1.getClass().getMethods()) {
        if ((m1.getName().startsWith("get") || m1.getName().startsWith("is")) && m1.getParameterCount() == 0 && !"getClass".equals(m1.getName())) {
          String fieldName = StringUtils.uncapitalize(m1.getName().replaceFirst("^(?:get|is)", ""));
          if (ignoredFieldSet.contains(fieldName)) {
            continue;
          }
          Method m2 = null;
          try {
            m2 = Arrays.stream(o2.getClass().getMethods()).filter(m -> m.getName().equalsIgnoreCase(m1.getName()) && m.getParameterCount() == m1.getParameterCount()).findFirst().orElse(null);

            if (m2 == null) {
              throw new NoSuchMethodException();
            }

            Object v1 = m1.invoke(o1);
            Object v2 = m2.invoke(o2);

            boolean result = true;

            Assertions.assertFalse(v1 == null ^ v2 == null, String.format("Both objects have to be null or not null:%n%s = %s%n%s = %s", m1, v1 == null ? "null" : v1.getClass().getName(), m2, v2 == null ? "null" : v2.getClass().getName()));
            if (v1 != null) {
              if (v1.equals(v2)) {
                //Do Nothing
              } else if (v1 instanceof Comparable && v2 instanceof Comparable && ((Comparable) v1).compareTo(v2) == 0) {
                //Do Nothing
              } else if (OffsetDateTime.class.isAssignableFrom(v1.getClass()) && OffsetDateTime.class.isAssignableFrom(v2.getClass())) {
                result = ((OffsetDateTime) v1).isEqual((OffsetDateTime) v2);
              } else if (ChronoZonedDateTime.class.isAssignableFrom(v1.getClass()) && ChronoZonedDateTime.class.isAssignableFrom(v2.getClass())) {
                result = ((ChronoZonedDateTime<?>) v1).isEqual((ChronoZonedDateTime<?>) v2);
              } else if (v1.getClass().isAssignableFrom(v2.getClass()) && ((v1.getClass().isPrimitive() && v2.getClass().isPrimitive()) || (hasStandardEquals(v1.getClass()) && hasStandardEquals(v2.getClass())))) {
                result = false;
              } else if (BigInteger.class.isAssignableFrom(v1.getClass()) && Integer.class.isAssignableFrom(v2.getClass())) {
                result = ((BigInteger) v1).intValue() == ((int) v2);
              } else if (BigInteger.class.isAssignableFrom(v2.getClass()) && Integer.class.isAssignableFrom(v1.getClass())) {
                result = ((BigInteger) v2).intValue() == ((int) v1);
              } else if (BigInteger.class.isAssignableFrom(v1.getClass()) && Long.class.isAssignableFrom(v2.getClass())) {
                result = ((BigInteger) v1).longValue() == ((long) v2);
              } else if (BigInteger.class.isAssignableFrom(v2.getClass()) && Long.class.isAssignableFrom(v1.getClass())) {
                result = ((BigInteger) v2).longValue() == ((long) v1);
              } else if (String.class.isAssignableFrom(v1.getClass()) && Enum.class.isAssignableFrom(v2.getClass())) {
                v2 = ReflectionUtils.enum2String((Enum<?>) v2);
                result = v1.equals(v2);
              } else if (String.class.isAssignableFrom(v2.getClass()) && Enum.class.isAssignableFrom(v1.getClass())) {
                v1 = ReflectionUtils.enum2String((Enum<?>) v1);
                result = v2.equals(v1);
              } else {
                boolean equals = v1.toString().equals(v2.toString());
                if (Enum.class.isAssignableFrom(v2.getClass()) && Enum.class.isAssignableFrom(v1.getClass())) {
                  result = equals;
                } else if (String.class.isAssignableFrom(v1.getClass()) || String.class.isAssignableFrom(v2.getClass())) {
                  result = equals;
                } else {
                  checked.addAll(reflectionEqualsByName(v1, v2));
                }
              }

              checked.add(m1);
            }

            Assertions.assertTrue(result, String.format("Invalid compare between methods%n%s = %s%n%s = %s", m1, v1, m2, v2));
          } catch (NoSuchMethodException e) {
            log.warn("Method {} is not defined in {}{}", m1, o2.getClass().getName(), e.getMessage());
          } catch (IllegalAccessException |
                   InvocationTargetException e) {
            throw new IllegalStateException(String.format("[ERROR] Something gone wrong comparing %s with %s%n%s", m1, m2, e.getMessage()));
          }
        }
      }
    }
    return checked;
  }

  private static boolean hasStandardEquals(Class<?> clazz) {
    try {
      return !clazz.getMethod("equals", Object.class).equals(Object.class.getMethod("equals", Object.class));
    } catch (NoSuchMethodException e) {
      // This exception cannot be thrown
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
