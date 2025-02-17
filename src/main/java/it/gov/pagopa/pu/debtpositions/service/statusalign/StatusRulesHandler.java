package it.gov.pagopa.pu.debtpositions.service.statusalign;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public abstract class StatusRulesHandler<E extends Enum<E>, T, D> {

  private final E syncStatus;
  private final E paidStatus;
  private final E unpaidStatus;
  private final E expiredStatus;
  private final E cancelledStatus;
  private final E reportedStatus;

  private final Set<E> allowedCancelledStatuses;
  private final Set<E> emptyAllowedStatuses;

  protected StatusRulesHandler(E syncStatus, E paidStatus, E unpaidStatus, E expiredStatus, E cancelledStatus, E reportedStatus) {
    this.syncStatus = syncStatus;
    this.paidStatus = paidStatus;
    this.unpaidStatus = unpaidStatus;
    this.expiredStatus = expiredStatus;
    this.cancelledStatus = cancelledStatus;
    this.reportedStatus = reportedStatus;

    this.allowedCancelledStatuses = Set.of(cancelledStatus);
    this.emptyAllowedStatuses = Set.of();
  }

  public void updateEntityStatus(T entity) {
    List<E> childStatuses = getChildStatuses(entity);
    log.info("Calculating new Status for {}", entity.getClass());
    D newStatus = calculateNewStatus(childStatuses);
    setStatus(entity, newStatus);
    log.info("Updating entity {} with status {}", entity.getClass(), newStatus);
    storeStatus(entity, newStatus);
  }

  protected abstract List<E> getChildStatuses(T entity);

  protected abstract D calculateNewStatus(List<E> childStatuses);

  protected abstract void setStatus(T entity, D newStatus);

  protected abstract void storeStatus(T entity, D newStatus);

  public boolean isToSync(List<E> childrenStatusList) {
    return childrenStatusList.contains(syncStatus);
  }

  protected abstract boolean isPartiallyPaid(List<E> childrenStatusList);

  public boolean isUnpaid(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, unpaidStatus, allowedCancelledStatuses);
  }

  public boolean isPaid(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, paidStatus, allowedCancelledStatuses);
  }

  public boolean isReported(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, reportedStatus, allowedCancelledStatuses);
  }

  public boolean isCancelled(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, cancelledStatus, emptyAllowedStatuses);
  }

  public boolean isExpired(List<E> childrenStatusList) {
    return allMatch(childrenStatusList, expiredStatus, emptyAllowedStatuses);
  }

  private boolean allMatch(List<E> statusList, E requiredState, Set<E> allowedStatuses) {
    if (statusList.isEmpty()) {
      return false;
    }
    return statusList.contains(requiredState) &&
      statusList.stream().allMatch(status -> requiredState.equals(status) || allowedStatuses.contains(status));
  }
}
