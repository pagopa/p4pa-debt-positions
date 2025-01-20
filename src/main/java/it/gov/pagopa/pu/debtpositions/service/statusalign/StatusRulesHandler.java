package it.gov.pagopa.pu.debtpositions.service.statusalign;

import java.util.List;
import java.util.Set;

public abstract class StatusRulesHandler<E extends Enum<E>, T, D> {

  private final E syncStatus;
  private final E paidStatus;
  private final E unpaidStatus;
  private final E expiredStatus;
  private final E cancelledStatus;
  private final E reportedStatus;
  private final E invalidStatus;

  protected StatusRulesHandler(E syncStatus, E paidStatus, E unpaidStatus, E expiredStatus, E cancelledStatus, E reportedStatus, E invalidStatus) {
    this.syncStatus = syncStatus;
    this.paidStatus = paidStatus;
    this.unpaidStatus = unpaidStatus;
    this.expiredStatus = expiredStatus;
    this.cancelledStatus = cancelledStatus;
    this.reportedStatus = reportedStatus;
    this.invalidStatus = invalidStatus;
  }

  public void updateEntityStatus(T entity) {
    List<E> childStatuses = getChildStatuses(entity);
    D newStatus = calculateNewStatus(childStatuses);
    setStatus(entity, newStatus);
    storeStatus(entity, newStatus);
  }

  protected abstract List<E> getChildStatuses(T entity);

  protected abstract D calculateNewStatus(List<E> childStatuses);

  protected abstract void setStatus(T entity, D newStatus);

  protected abstract void storeStatus(T entity, D newStatus);

  public boolean isToSync(List<E> childrenStatusList) {
    return childrenStatusList.contains(syncStatus);
  }

  public boolean isPartiallyPaid(List<E> childrenStatusList) {
    return childrenStatusList.contains(paidStatus) &&
      (childrenStatusList.contains(unpaidStatus) || childrenStatusList.contains(expiredStatus));
  }

  public boolean isUnpaid(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, unpaidStatus, Set.of(cancelledStatus));
  }

  public boolean isPaid(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, paidStatus, Set.of(cancelledStatus));
  }

  public boolean isReported(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, reportedStatus, Set.of(cancelledStatus));
  }

  public boolean isInvalid(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, invalidStatus, Set.of(cancelledStatus));
  }

  public boolean isCancelled(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, cancelledStatus, Set.of());
  }

  public boolean isExpired(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, expiredStatus, Set.of());
  }

  private boolean allMatch(List<E> statusList, E requiredState, Set<E> allowedStatuses) {
    if (statusList.isEmpty()) {
      return false;
    }
    return statusList.contains(requiredState) &&
      statusList.stream().allMatch(status -> requiredState.equals(status) || allowedStatuses.contains(status));
  }
}
