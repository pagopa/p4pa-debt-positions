package it.gov.pagopa.pu.debtpositions.service.massive;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.massive.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.debtpositions.service.massive.action.InsertionActionMassiveDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.massive.action.UpdateActionMassiveDebtPositionService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class DebtPositionMassiveServiceImpl implements DebtPositionMassiveService {

  private final InstallmentSynchronizeMapper installmentSynchronizeMapper;
  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeRepository debtPositionTypeRepository;
  private final OrganizationSearchClient organizationSearchClient;
  private final InsertionActionMassiveDebtPositionService insertionActionMassiveDebtPositionService;
  private final UpdateActionMassiveDebtPositionService updateActionMassiveDebtPositionService;

  public DebtPositionMassiveServiceImpl(InstallmentSynchronizeMapper installmentSynchronizeMapper, DebtPositionRepository debtPositionRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionTypeRepository debtPositionTypeRepository, OrganizationSearchClient organizationSearchClient, InsertionActionMassiveDebtPositionService insertionActionMassiveDebtPositionService, UpdateActionMassiveDebtPositionService updateActionMassiveDebtPositionService) {
    this.installmentSynchronizeMapper = installmentSynchronizeMapper;
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.organizationSearchClient = organizationSearchClient;
    this.insertionActionMassiveDebtPositionService = insertionActionMassiveDebtPositionService;
    this.updateActionMassiveDebtPositionService = updateActionMassiveDebtPositionService;
  }


  @Override
  public String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionSynchronizeDTO = createDpFromInstallmentSynchronize(installmentSynchronizeDTO, accessToken);

    DebtPosition debtPosition = checkProcessableDebtPosition(debtPositionSynchronizeDTO.getIupdOrg());

    String workflowId = null;
    InstallmentSynchronizeDTO.ActionEnum action = installmentSynchronizeDTO.getAction();
    switch (action) {
      case InstallmentSynchronizeDTO.ActionEnum.I ->
        workflowId = insertionActionMassiveDebtPositionService.handleInsertion(debtPositionSynchronizeDTO, debtPosition, accessToken, operatorExternalUserId);
      case InstallmentSynchronizeDTO.ActionEnum.M ->
        workflowId = updateActionMassiveDebtPositionService.handleModification(debtPositionSynchronizeDTO, accessToken);
      case InstallmentSynchronizeDTO.ActionEnum.A ->
        workflowId = updateActionMassiveDebtPositionService.handleCancellation(debtPositionSynchronizeDTO, accessToken);
    }

    //dopo rischedulazione workflow scadenza

    return workflowId;
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(
        organizationId, debtPositionTypeCode)
      .orElseThrow(() -> new InvalidValueException(String.format("The debt position type code %s is not valid for this organizationId %s", debtPositionTypeCode, organizationId)));
  }

  private void populateFirstTransfer(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg) {
    Long organizationId = installmentSynchronizeDTO.getOrganizationId();

    Organization organization = organizationSearchClient.findByOrganizationId(organizationId, accessToken);
    String category = debtPositionTypeRepository.findByDebtPositionTypeId(debtPositionTypeOrg.getDebtPositionTypeId()).getTaxonomyCode();

    BigDecimal totalAmountOtherTransfers = installmentSynchronizeDTO.getTransfersList().stream()
      .filter(Objects::nonNull).map(TransferSynchronizeDTO::getAmount)
      .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

    TransferSynchronizeDTO firstTransfer = TransferSynchronizeDTO.builder()
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(debtPositionTypeOrg.getIban())
      .category(category)
      .amount(installmentSynchronizeDTO.getAmount().min(totalAmountOtherTransfers))
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .build();

    installmentSynchronizeDTO.getTransfersList().add(firstTransfer);
  }

  private DebtPositionDTO createDpFromInstallmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = retrieveDebtPositionTypeOrg(installmentSynchronizeDTO.getOrganizationId(),
      installmentSynchronizeDTO.getDebtPositionTypeCode());

    populateFirstTransfer(installmentSynchronizeDTO, accessToken, debtPositionTypeOrg);

    if(installmentSynchronizeDTO.getBalance().isBlank()){
      installmentSynchronizeDTO.setBalance(debtPositionTypeOrg.getBalance());
    }

    return installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
  }


  private DebtPosition checkProcessableDebtPosition(String iupdOrg) {
    Optional<DebtPosition> debtPosition = debtPositionRepository.findByIupdOrg(iupdOrg);

    if (debtPosition.isPresent() && !DebtPositionOrigin.ORDINARY_SIL.equals(debtPosition.get().getDebtPositionOrigin())) {
      throw new ConflictErrorException(String.format("There is another debt position with iupd %s requested but different origin", iupdOrg));
    }
    return debtPosition.orElse(null);
  }

}

