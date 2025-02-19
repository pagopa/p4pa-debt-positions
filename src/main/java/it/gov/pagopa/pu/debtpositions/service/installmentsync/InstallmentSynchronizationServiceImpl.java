package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class InstallmentSynchronizationServiceImpl implements InstallmentSynchronizationService {

  private final InstallmentSynchronizeMapper installmentSynchronizeMapper;
  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeRepository debtPositionTypeRepository;
  private final OrganizationSearchClient organizationSearchClient;
  private final InsertInstallmentServiceImpl insertInstallmentService;

  public InstallmentSynchronizationServiceImpl(InstallmentSynchronizeMapper installmentSynchronizeMapper, DebtPositionRepository debtPositionRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionTypeRepository debtPositionTypeRepository, OrganizationSearchClient organizationSearchClient, InsertInstallmentServiceImpl insertInstallmentService) {
    this.installmentSynchronizeMapper = installmentSynchronizeMapper;
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.organizationSearchClient = organizationSearchClient;
    this.insertInstallmentService = insertInstallmentService;
  }

  @Override
  @Transactional
  public String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, Boolean massive, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionSynchronizeDTO = createDpFromInstallmentSynchronize(installmentSynchronizeDTO, accessToken);

    DebtPosition debtPosition = retrieveAndVerifyOrigin(debtPositionSynchronizeDTO.getIupdOrg());

    InstallmentSynchronizeDTO.ActionEnum action = installmentSynchronizeDTO.getAction();
    return switch (action) {
      case InstallmentSynchronizeDTO.ActionEnum.I ->
        insertInstallmentService.handleInsertion(debtPositionSynchronizeDTO, debtPosition, massive, accessToken, operatorExternalUserId);
      case InstallmentSynchronizeDTO.ActionEnum.M -> "";
        // add handle update
      case InstallmentSynchronizeDTO.ActionEnum.A -> "";
        // add handle cancellation
    };

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
      .map(TransferSynchronizeDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

    TransferSynchronizeDTO firstTransfer = TransferSynchronizeDTO.builder()
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(debtPositionTypeOrg.getIban().isBlank() ? organization.getIban() : debtPositionTypeOrg.getIban())
      .category(category)
      .amount(installmentSynchronizeDTO.getAmount().min(totalAmountOtherTransfers))
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .build();

    installmentSynchronizeDTO.getTransfersList().add(firstTransfer);
  }

  private DebtPosition retrieveAndVerifyOrigin(String iupdOrg) {
    Optional<DebtPosition> debtPosition = debtPositionRepository.findByIupdOrg(iupdOrg);

    if (debtPosition.map(d -> !DebtPositionOrigin.ORDINARY_SIL.equals(d.getDebtPositionOrigin())).orElse(false)) {
      throw new ConflictErrorException(String.format("There is another debt position with iupd %s requested but different origin", iupdOrg));
    }
    return debtPosition.orElse(null);
  }

}
