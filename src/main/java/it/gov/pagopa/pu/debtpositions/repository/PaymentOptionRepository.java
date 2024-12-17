package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "payment_option", path = "paymentOption")
public interface PaymentOptionRepository extends JpaRepository<PaymentOption,Long> {

}
