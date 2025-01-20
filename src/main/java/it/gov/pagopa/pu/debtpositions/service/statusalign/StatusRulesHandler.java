package it.gov.pagopa.pu.debtpositions.service.statusalign;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class StatusRulesHandler<E extends Enum<E>> {

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

  public <T, S, C extends Enum<C>> void updateEntityStatus(T entity,
                                                           Function<T, List<S>> childStatusExtractor,
                                                           Function<List<S>, C> statusDeterminer,
                                                           BiConsumer<T, C> statusUpdater,
                                                           BiConsumer<T, C> repositoryUpdater) {

    List<S> childStatuses = childStatusExtractor.apply(entity);
    C newStatus = statusDeterminer.apply(childStatuses);
    statusUpdater.accept(entity, newStatus);
    repositoryUpdater.accept(entity, newStatus);
  }

  public boolean isToSync(List<E> statusList) {
    return statusList.contains(syncStatus);
  }

  public boolean isPartiallyPaid(List<E> statusList) {
    return statusList.contains(paidStatus) &&
      (statusList.contains(unpaidStatus) || statusList.contains(expiredStatus));
  }

  public boolean isUnpaid(List<E> statusList) {
    return allMatchOrEmpty(statusList, status -> status.equals(unpaidStatus)) ||
      (allMatchOrEmpty(statusList, status -> status.equals(cancelledStatus) || status.equals(unpaidStatus)) &&
        statusList.contains(unpaidStatus));
  }

  public boolean isPaid(List<E> statusList) {
    return allMatchOrEmpty(statusList, status -> status.equals(paidStatus)) ||
      (allMatchOrEmpty(statusList, status -> status.equals(cancelledStatus) || status.equals(paidStatus)) &&
        statusList.contains(paidStatus));
  }

  public boolean isReported(List<E> statusList) {
    return allMatchOrEmpty(statusList, status -> status.equals(reportedStatus)) ||
      (allMatchOrEmpty(statusList, status -> status.equals(cancelledStatus) || status.equals(reportedStatus)) &&
        statusList.contains(reportedStatus));
  }

  public boolean isInvalid(List<E> statusList) {
    return allMatchOrEmpty(statusList, status -> status.equals(invalidStatus)) ||
      (allMatchOrEmpty(statusList, status -> status.equals(cancelledStatus) || status.equals(invalidStatus)) &&
        statusList.contains(invalidStatus));
  }

  public boolean isCancelled(List<E> statusList) {
    return allMatchOrEmpty(statusList, status -> status.equals(cancelledStatus));
  }

  public boolean isExpired(List<E> statusList) {
    return allMatchOrEmpty(statusList, status -> status.equals(expiredStatus));
  }

  private boolean allMatchOrEmpty(List<E> statusList, Predicate<E> predicate) {
    if (statusList.isEmpty()) {
      return false;
    }
    return statusList.stream().allMatch(predicate);
  }
}
