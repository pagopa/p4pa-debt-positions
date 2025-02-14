package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidInstallmentStatusException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PrimaryOrgInstallmentService {

  private enum CHECK_MODE {EXACTLY_ONE, MOST_RECENT}

  private final InstallmentNoPIIRepository installmentNoPIIRepository;

  public PrimaryOrgInstallmentService(InstallmentNoPIIRepository installmentNoPIIRepository) {
    this.installmentNoPIIRepository = installmentNoPIIRepository;
  }

  public Optional<InstallmentNoPII> findPrimaryOrgInstallment(Organization primaryOrg, String noticeNumber){
    // check installments by orgId/noticeNumber
    List<InstallmentNoPII> fullInstallmentList = installmentNoPIIRepository.getByOrganizationIdAndNav(primaryOrg.getOrganizationId(), noticeNumber);

    //if no installment is found, then a new debt position must be created, just like the case of secondary-org transfer
    if (!fullInstallmentList.isEmpty()) {

      // filter out installments with status PAID and REPORTED
      List<InstallmentNoPII> installmentList = fullInstallmentList.stream()
        .filter(anInstallment -> !anInstallment.getStatus().equals(InstallmentStatus.REPORTED)
          && !anInstallment.getStatus().equals(InstallmentStatus.PAID)).toList();

      /*
       * Apply these rules to find if a valid installment (i.e. not discarded by status PAID or REPORTED) is associated to the receipt:
       * 1. >1 installments with status UNPAID -> throw exception
       * 2. 1 installment with status UNPAID -> use it
       * 3. >1 installment with status TO_SYNC and sync_status_to UNPAID -> throw exception
       * 4. 1 installment with status TO_SYNC and sync_status_to UNPAID -> use it
       * 5. >=1 installments with status EXPIRED -> use the most recent (i.e. with the most recent due date)
       * 6. (default) -> not found (nothing to do)
       */
      Optional<InstallmentNoPII> primaryOrgInstallment = checkValidInstallment(installmentList, CHECK_MODE.EXACTLY_ONE, InstallmentStatus.UNPAID, null)
        .or(() -> checkValidInstallment(installmentList, CHECK_MODE.EXACTLY_ONE, InstallmentStatus.TO_SYNC, InstallmentStatus.UNPAID))
        .or(() -> checkValidInstallment(installmentList, CHECK_MODE.MOST_RECENT, InstallmentStatus.EXPIRED, null));

      if(primaryOrgInstallment.isEmpty()){
        //case no valid installment found to associate to receipt, but no exception found (for instance: all installment in status paid/reported)
        //in this case, just log the event
        List<String> installmentWithStatusList = fullInstallmentList.stream().map(i -> i.getInstallmentId() + "/" +
          (i.getStatus().equals(InstallmentStatus.TO_SYNC) ? (i.getSyncStatus().getSyncStatusFrom() + "->" + i.getSyncStatus().getSyncStatusTo()) : i.getSyncStatus())).toList();
        PrimaryOrgInstallmentService.log.info("No valid installment found to associate to receipt [{}/{}]; list of installment/status [{}]", primaryOrg.getOrgFiscalCode() ,noticeNumber, installmentWithStatusList);
      }

      return primaryOrgInstallment;
    } else {
      return Optional.empty();
    }
  }

  private Optional<InstallmentNoPII> checkValidInstallment(List<InstallmentNoPII> installmentList, CHECK_MODE checkMode, InstallmentStatus status, InstallmentStatus syncStatusTo) {
    return installmentList.stream()
      .filter(installmentDTO -> status.equals(installmentDTO.getStatus())
        && (syncStatusTo == null || (installmentDTO.getSyncStatus() != null && syncStatusTo.equals(installmentDTO.getSyncStatus().getSyncStatusTo()))))
      .reduce((a, b) -> {
        if (checkMode.equals(CHECK_MODE.MOST_RECENT)) {
          return a.getDueDate().isAfter(b.getDueDate()) ? a : b;
        } else {  //mode EXACTLY_ONE
          throw new InvalidInstallmentStatusException("Multiple installments with status " + status
            + (syncStatusTo != null ? " and syncStatusTo " + syncStatusTo : ""));
        }
      });
  }

}
