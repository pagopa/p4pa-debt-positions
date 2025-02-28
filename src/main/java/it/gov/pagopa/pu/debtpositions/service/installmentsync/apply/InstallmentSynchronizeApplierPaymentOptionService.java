package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Service
public class InstallmentSynchronizeApplierPaymentOptionService {

  public void merge(InstallmentSynchronizeDTO installmentSynchronizeDTO, PaymentOptionDTO paymentOptionDTO){
    paymentOptionDTO.setDescription(installmentSynchronizeDTO.getDescription());

    Set<String> modifiedFields = new HashSet<>();
    checkImmutableField("paymentOptionIndex", installmentSynchronizeDTO.getPaymentOptionIndex(), paymentOptionDTO.getPaymentOptionIndex(), modifiedFields);
    checkImmutableField("paymentOptionType", installmentSynchronizeDTO.getPaymentOptionType(), paymentOptionDTO.getPaymentOptionType(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException("These fields are not mutable: " + modifiedFields);
    }
  }
}
