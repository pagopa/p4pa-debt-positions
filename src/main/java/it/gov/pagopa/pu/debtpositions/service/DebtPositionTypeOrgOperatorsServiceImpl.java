package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
  public int deleteOperatorsByDebtPositionTypeOrgId(Long debtPositionTypeOrgId) {
    Integer deletedOperators = debtPositionTypeOrgOperatorsRepository.deleteByDebtPositionTypeOrgId(
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
    //Check for already existing DebtPositionTypeOrgOperators
    List<DebtPositionTypeOrgOperators> debtPositionTypeOrgOperatorsList = debtPositionTypeOrgOperatorsRepository.findByDebtPositionTypeOrgId(
      debtPositionTypeOrgId);
    if(!CollectionUtils.isEmpty(debtPositionTypeOrgOperatorsList)){
      externalOperatorUserIds.removeIf(o->debtPositionTypeOrgOperatorsList.stream().anyMatch(dptoo->dptoo.getOperatorExternalUserId().equals(o)));
    }
    if(CollectionUtils.isEmpty(externalOperatorUserIds)){
      return Collections.emptyList();
    }

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
  public List<DebtPositionTypeOrgOperators> saveDebtPositionTypeOrgOperatorsForOperator(
    String operatorExternalUserId, Set<Long> debtPositionTypeOrgIds) {

    List<DebtPositionTypeOrgOperators> debtPositionTypeOrgOperatorsList = debtPositionTypeOrgOperatorsRepository.findByOperatorExternalUserId(operatorExternalUserId);

    if(!CollectionUtils.isEmpty(debtPositionTypeOrgOperatorsList)){
      debtPositionTypeOrgIds.removeIf(o->debtPositionTypeOrgOperatorsList.stream().anyMatch(dptoo->dptoo.getDebtPositionTypeOrgId().equals(o)));
    }

    if(CollectionUtils.isEmpty(debtPositionTypeOrgIds)){
      return Collections.emptyList();
    }

    return debtPositionTypeOrgOperatorsRepository.saveAll(
      debtPositionTypeOrgIds.stream().map(o->{
        DebtPositionTypeOrgOperators debtPositionTypeOrgOperators = new DebtPositionTypeOrgOperators();
        debtPositionTypeOrgOperators.setDebtPositionTypeOrgId(o);
        debtPositionTypeOrgOperators.setOperatorExternalUserId(operatorExternalUserId);
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
