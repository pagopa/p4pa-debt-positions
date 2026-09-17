package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.RelateUserToDefaultDPTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class DefaultDpTypeOrgOperatorFacadeServiceImpl implements DefaultDpTypeOrgOperatorFacadeService {
  private final OrganizationService organizationService;
  private final it.gov.pagopa.pu.debtpositions.connector.migration.service.DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;
  private final DebtPositionTypeOrgOperatorsFacadeService dptoService;

  public DefaultDpTypeOrgOperatorFacadeServiceImpl(OrganizationService organizationService, it.gov.pagopa.pu.debtpositions.connector.migration.service.DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService, DebtPositionTypeOrgOperatorsFacadeService dptoService) {
    this.organizationService = organizationService;
    this.debtPositionTypeOrgOperatorsService = debtPositionTypeOrgOperatorsService;
    this.dptoService = dptoService;
  }

  @Override
  public List<DebtPositionTypeOrgOperators> relateUserToDefaultDPTypeOrg(RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO, String accessToken) {
    Organization organization = organizationService.getOrganizationById(relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), accessToken)
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_NOT_FOUND, "Organization having id " + relateUserToDefaultDPTypeOrgDTO.getOrganizationId() + " not found"));
    List<Long> unconsumedDebtPositionTypeOrgIds = new ArrayList<>();
    if(StringUtils.isNotBlank(relateUserToDefaultDPTypeOrgDTO.getFiscalCode())) {
      unconsumedDebtPositionTypeOrgIds = debtPositionTypeOrgOperatorsService.getUnconsumedDebtPositionTypeOrgIds(
        relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), relateUserToDefaultDPTypeOrgDTO.getFiscalCode(), organization.getIpaCode()
      );
    }
    List<DebtPositionTypeOrgOperators> operators = dptoService.saveDebtPositionTypeOrgOperatorsForNewOperator(
      relateUserToDefaultDPTypeOrgDTO.getOperatorExternalUserId(), relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), new HashSet<>(unconsumedDebtPositionTypeOrgIds)
    );
    if(!CollectionUtils.isEmpty(unconsumedDebtPositionTypeOrgIds)) {
      debtPositionTypeOrgOperatorsService.consumeDebtPositionTypeOrgOperators(
        relateUserToDefaultDPTypeOrgDTO.getOrganizationId(), unconsumedDebtPositionTypeOrgIds, relateUserToDefaultDPTypeOrgDTO.getFiscalCode(), organization.getIpaCode()
      );
    }
    return operators;
  }
}
