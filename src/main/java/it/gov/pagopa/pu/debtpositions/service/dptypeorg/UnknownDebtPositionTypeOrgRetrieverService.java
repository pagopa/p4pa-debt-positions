package it.gov.pagopa.pu.debtpositions.service.dptypeorg;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionTypeOrgMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UnknownDebtPositionTypeOrgRetrieverService {

  public static final Long DEBT_POSITION_TYPE_UNKNOWN = -1L;
  private DebtPositionType debtPositionType;


  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgMapper debtPositionTypeOrgMapper;
  private final DebtPositionTypeRepository debtPositionTypeRepository;

  private final Map<Long, DebtPositionTypeOrg> debtPositionTypeOrgSecondaryOrgMap = new ConcurrentHashMap<>();

  public UnknownDebtPositionTypeOrgRetrieverService(
    DebtPositionTypeRepository debtPositionTypeRepository,
    DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
    DebtPositionTypeOrgMapper debtPositionTypeOrgMapper
  ) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.debtPositionTypeOrgMapper = debtPositionTypeOrgMapper;
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public DebtPositionTypeOrg getUnknownDebtPositionTypeOrg(Long organizationId) {
    return debtPositionTypeOrgSecondaryOrgMap.computeIfAbsent(organizationId,this::retrieveDebtPositionTypeOrg);
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(organizationId, getDebtPositionTypeSecondaryOrg().getCode())
      .orElseGet(() -> createDebtPositionTypeOrg(organizationId));
  }

  private DebtPositionTypeOrg createDebtPositionTypeOrg(Long organizationId) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgMapper.mapFromDebtPositionType(debtPositionType, organizationId);
    //set specific fields of debtPositionTypeOrg
    //flagActive is set to false because it's not possible to create ordinary debt positions with debtPositionType UNKNOWN
    debtPositionTypeOrg.setFlagActive(false);

    debtPositionTypeOrg = debtPositionTypeOrgRepository.save(debtPositionTypeOrg);
    log.info("debtPositionTypeOrg UNKNOWN created for organizationId[{}]: id[[{}]", organizationId, debtPositionTypeOrg.getDebtPositionTypeId());
    return debtPositionTypeOrg;
  }

  private DebtPositionType getDebtPositionTypeSecondaryOrg() {
    if(debtPositionType==null){
      debtPositionType = debtPositionTypeRepository.findById(DEBT_POSITION_TYPE_UNKNOWN)
        .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_NOT_FOUND, "DebtPositionType with id " + DEBT_POSITION_TYPE_UNKNOWN + " not found"));
      log.info("debt position type code UNKNOWN: {}", debtPositionType.getCode());
    }
    return debtPositionType;
  }

}
