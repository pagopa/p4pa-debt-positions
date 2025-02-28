package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionTypeOrgMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DebtPositionTypeOrgSecondaryOrgRetrieverService {

  public static final Long DEBT_POSITION_TYPE_SECONDARY_ORG = -1L;
  private DebtPositionType debtPositionType;


  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgMapper debtPositionTypeOrgMapper;
  private final DebtPositionTypeRepository debtPositionTypeRepository;

  private final Map<Long, DebtPositionTypeOrg> debtPositionTypeOrgSecondaryOrgMap = new ConcurrentHashMap<>();

  public DebtPositionTypeOrgSecondaryOrgRetrieverService(
    DebtPositionTypeRepository debtPositionTypeRepository,
    DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
    DebtPositionTypeOrgMapper debtPositionTypeOrgMapper
  ) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.debtPositionTypeOrgMapper = debtPositionTypeOrgMapper;
  }

  public DebtPositionTypeOrg getSecondaryOrgDebtPositionTypeOrg(Long organizationId) {
    return debtPositionTypeOrgSecondaryOrgMap.computeIfAbsent(organizationId,this::retrieveDebtPositionTypeOrg);
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(organizationId, getDebtPositionTypeSecondaryOrg().getCode())
      .orElseGet(() -> createDebtPositionTypeOrg(organizationId));
  }

  private DebtPositionTypeOrg createDebtPositionTypeOrg(Long organizationId) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgMapper.mapFromDebtPositionType(debtPositionType, organizationId);
    debtPositionTypeOrg = debtPositionTypeOrgRepository.save(debtPositionTypeOrg);
    log.info("debtPositionTypeOrg for secondary org created for organizationId[{}]: id[[{}]", organizationId, debtPositionTypeOrg.getDebtPositionTypeId());
    return debtPositionTypeOrg;
  }

  private DebtPositionType getDebtPositionTypeSecondaryOrg() {
    if(debtPositionType==null){
      debtPositionType = debtPositionTypeRepository.findById(DEBT_POSITION_TYPE_SECONDARY_ORG)
        .orElseThrow(() -> new NotFoundException("Debt position type for secondary org with id[" + DEBT_POSITION_TYPE_SECONDARY_ORG + "] not found"));
      log.info("debt position type code for secondary org: {}", debtPositionType.getCode());
    }
    return debtPositionType;
  }

}
