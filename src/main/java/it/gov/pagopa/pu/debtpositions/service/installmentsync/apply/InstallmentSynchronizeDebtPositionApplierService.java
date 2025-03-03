package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Service
public class InstallmentSynchronizeDebtPositionApplierService {

  public void merge(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.setDescription(installmentSynchronizeDTO.getDescription());
    debtPositionDTO.setValidityDate(installmentSynchronizeDTO.getValidityDate());

    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("multiDebtor", installmentSynchronizeDTO.getMultiDebtor(), debtPositionDTO.getMultiDebtor(), modifiedFields);
    checkImmutableField("flagPagoPaPayment", installmentSynchronizeDTO.getFlagPagoPaPayment(), debtPositionDTO.getFlagPagoPaPayment(), modifiedFields);

    if (!modifiedFields.isEmpty()) {
      throw new ConflictErrorException(String.format("These fields for debt position with iupd %s are not mutable: %s", debtPositionDTO.getIupdOrg(), modifiedFields));
    }
  }
}
