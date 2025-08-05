package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Service
public class InstallmentSynchronizePaymentOptionApplierService {

  public void merge(InstallmentSynchronizeDTO installmentSynchronizeDTO, PaymentOptionDTO paymentOptionDTO){
    paymentOptionDTO.setDescription(installmentSynchronizeDTO.getPaymentOptionDescription());

    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("paymentOptionIndex", installmentSynchronizeDTO.getPaymentOptionIndex(), paymentOptionDTO.getPaymentOptionIndex(), modifiedFields);
    checkImmutableField("paymentOptionType", installmentSynchronizeDTO.getPaymentOptionType(), paymentOptionDTO.getPaymentOptionType().getValue(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException(String.format("[P4PA_UNMODIFIABLE_FIELD] These fields for payment option with index %s of debt position with iupd %s are not mutable: %s", paymentOptionDTO.getPaymentOptionIndex(), installmentSynchronizeDTO.getIupdOrg(), modifiedFields));
    }
  }
}
