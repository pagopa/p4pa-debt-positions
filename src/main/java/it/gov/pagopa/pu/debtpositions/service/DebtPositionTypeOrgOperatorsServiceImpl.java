package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DebtPositionTypeOrgOperatorsServiceImpl implements DebtPositionTypeOrgOperatorsService{

  private final DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository;

  public DebtPositionTypeOrgOperatorsServiceImpl(
    DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository) {
    this.debtPositionTypeOrgOperatorsRepository = debtPositionTypeOrgOperatorsRepository;
  }

  @Transactional
  @Override
  public long deleteOperatorsByDebtPositionTypeOrgId(Long debtPositionTypeOrgId) {
    long deletedOperators = debtPositionTypeOrgOperatorsRepository.deleteByDebtPositionTypeOrgId(
      debtPositionTypeOrgId);
    logDeletedOperators(debtPositionTypeOrgId, deletedOperators);
    return deletedOperators;
  }

  private static void logDeletedOperators(Long debtPositionTypeOrgId,
    long deletedOperators) {
    log.info("Deleted {} operators having debtPositionTypeOrgId {}",
      deletedOperators, debtPositionTypeOrgId);
  }

  @Transactional
  @Override
  public List<DebtPositionTypeOrgOperators> saveOperators(
    Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds) {
    return debtPositionTypeOrgOperatorsRepository.saveAll(
      externalOperatorUserIds.stream().map(o->{
        DebtPositionTypeOrgOperators debtPositionTypeOrgOperators = new DebtPositionTypeOrgOperators();
        debtPositionTypeOrgOperators.setDebtPositionTypeOrgId(debtPositionTypeOrgId);
        debtPositionTypeOrgOperators.setOperatorExternalUserId(o);
        return debtPositionTypeOrgOperators;
      }).toList()
    );
  }

  @Transactional
  @Override
  public int deleteOperators(Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds) {
    int deletedOperators = debtPositionTypeOrgOperatorsRepository.deleteByDebtPositionTypeOrgIdAndOperatorExternalUserId(
      debtPositionTypeOrgId,
      externalOperatorUserIds
    );
    if(externalOperatorUserIds.size()!=deletedOperators){
      log.warn("The number of deleted operators does not match the disabled operators. [disabledOperators:{}, deletedOperators:{}]", externalOperatorUserIds.size(),deletedOperators);
    }
    logDeletedOperators(debtPositionTypeOrgId, deletedOperators);
    return deletedOperators;
  }
}
