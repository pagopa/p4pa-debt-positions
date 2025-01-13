package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {
  @Column(updatable = false)
  @CreatedDate
  private LocalDateTime creationDate;
  @LastModifiedDate
  private LocalDateTime updateDate;
  @LastModifiedBy
  private String updateOperatorExternalId;
}
