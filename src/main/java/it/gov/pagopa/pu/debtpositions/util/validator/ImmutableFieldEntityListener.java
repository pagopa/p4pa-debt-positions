package it.gov.pagopa.pu.debtpositions.util.validator;

import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PreUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;

@Component
public class ImmutableFieldEntityListener extends AuditingEntityListener {

  @PersistenceContext
  private EntityManager entityManager;

  @PreUpdate
  public void preUpdate(Object entity) throws IllegalStateException {
    Object originalEntity = entityManager.find(
      entity.getClass(),
      getEntityId(entity)
    );

    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(ImmutableField.class)) {

        try {
          Object originalValue = field.get(originalEntity);
          Object newValue = field.get(entity);

          if (!Objects.equals(originalValue, newValue)) {
            throw new ConflictErrorException("Field " + field.getName() + " cannot be modified");
          }
        } catch (IllegalAccessException e) {
          throw new ConflictErrorException("Cannot access to field " + field.getName());
        }
      }
    }
  }

  private Object getEntityId(Object entity) {
    try {
      for (Field field : entity.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(Id.class)) {

          return field.get(entity);
        }
      }
    } catch (IllegalAccessException e) {
      throw new ConflictErrorException("Cannot access to fields for entity: " + entity);
    }
    throw new IllegalStateException("No @Id field found for entity: " +  entity);
  }
}
