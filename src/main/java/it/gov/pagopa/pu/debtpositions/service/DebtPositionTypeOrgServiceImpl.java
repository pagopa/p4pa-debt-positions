package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.workflow.DebtPositionTypeOrgMapper;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionTypeOrgServiceImpl implements DebtPositionTypeOrgService {
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgMapper debtPositionTypeOrgMapper;

  public DebtPositionTypeOrgServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionTypeOrgMapper debtPositionTypeOrgMapper) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeOrgMapper = debtPositionTypeOrgMapper;
  }

  @Override
  public DebtPositionTypeOrgDTO getDebtPositionTypeOrgByOrganizationIdAndCode(Long organizationId, String code) {
    return debtPositionTypeOrgMapper.map(debtPositionTypeOrgRepository.findByOrganizationIdAndCode(organizationId, code)
      .orElseThrow(() -> new NotFoundException("DebtPositionTypeOrg not found for organizationId " + organizationId + " with code " + code)));
  }
}
