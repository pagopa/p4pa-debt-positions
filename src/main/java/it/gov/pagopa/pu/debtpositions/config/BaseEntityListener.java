package it.gov.pagopa.pu.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.model.BaseEntity;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class BaseEntityListener {

  @PrePersist
  public void onPrePersist(BaseEntity entity) {
    onSave(entity);
  }

  @PreUpdate
  public void onPreUpdate(BaseEntity entity) {
    onSave(entity);
  }

  private void onSave(BaseEntity entity) {
    entity.setUpdateTraceId(Utilities.getTraceId());
  }
}
