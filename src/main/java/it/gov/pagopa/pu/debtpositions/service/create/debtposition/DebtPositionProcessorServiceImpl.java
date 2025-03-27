package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionProcessorServiceImpl implements DebtPositionProcessorService {

  private final DebtPositionTypeRepository debtPositionTypeRepository;

  public DebtPositionProcessorServiceImpl(DebtPositionTypeRepository debtPositionTypeRepository) {
    this.debtPositionTypeRepository = debtPositionTypeRepository;
  }

  @Override
  public DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      long totalPaymentOptionAmount = paymentOption.getInstallments().stream()
        .filter(this::isInstallmentCancelled)
        .mapToLong(InstallmentDTO::getAmountCents)
        .sum();

      paymentOption.setTotalAmountCents(totalPaymentOptionAmount);
    });

    return debtPositionDTO;
  }

  private boolean isInstallmentCancelled(InstallmentDTO installment){
    return !(installment.getStatus() == InstallmentStatus.CANCELLED ||
      (installment.getSyncStatus() != null && InstallmentStatus.CANCELLED.equals(installment.getSyncStatus().getSyncStatusTo())));
  }

  @Override
  public void populateFirstTransfer(InstallmentDTO installmentDTO, Organization organization, DebtPositionTypeOrg debtPositionTypeOrg) {
    String category = debtPositionTypeRepository.findById(debtPositionTypeOrg.getDebtPositionTypeId())
      .orElseThrow(() -> new NotFoundException(String.format("The debt position type with id %s is not found", debtPositionTypeOrg.getDebtPositionTypeId())))
      .getTaxonomyCode();

    Long totalAmountOtherTransfers = installmentDTO.getTransfers().stream()
      .mapToLong(TransferDTO::getAmountCents).sum();

    TransferDTO firstTransfer = TransferDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(debtPositionTypeOrg.getIban().isBlank() ? organization.getIban() : debtPositionTypeOrg.getIban())
      .category(category)
      .amountCents(installmentDTO.getAmountCents() - totalAmountOtherTransfers)
      .remittanceInformation(installmentDTO.getRemittanceInformation())
      .build();

    installmentDTO.addTransfersItem(firstTransfer);
  }

}
