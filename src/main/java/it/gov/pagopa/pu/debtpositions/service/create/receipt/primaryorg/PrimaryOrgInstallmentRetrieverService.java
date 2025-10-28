package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrimaryOrgInstallmentRetrieverService {

  private final InstallmentNoPIIRepository installmentNoPIIRepository;

  public PrimaryOrgInstallmentRetrieverService(InstallmentNoPIIRepository installmentNoPIIRepository) {
    this.installmentNoPIIRepository = installmentNoPIIRepository;
  }

  public Optional<InstallmentNoPII> retrieve(Organization primaryOrg, String nav, String iud) {
    if (StringUtils.isNotEmpty(iud)) {
      return findByIud(primaryOrg, nav, iud);
    } else {
      return findByNav(primaryOrg, nav);
    }
  }

  private Optional<InstallmentNoPII> findByNav(Organization primaryOrg, String nav) {
    List<InstallmentNoPII> installments = installmentNoPIIRepository.getByOrganizationIdAndNav(primaryOrg.getOrganizationId(), nav, InstallmentUtils.PRIMARY_ORG_DEBT_POSITION_ORIGINS)
      .stream()
      .filter(i -> !InstallmentStatus.CANCELLED.equals(i.getStatus()))
      .toList();
    if(installments.isEmpty()){
      return Optional.empty();
    } else if(installments.size() > 1){
      throw new ConflictErrorException("There are too many Instalments having NAV " + nav + " on organizationId " + primaryOrg.getOrganizationId() + ":" +
        getInstallmentDetails(installments)
      );
    } else {
      return Optional.of(installments.getFirst());
    }
  }

  private Optional<InstallmentNoPII> findByIud(Organization primaryOrg, String nav, String iud) {
    List<InstallmentNoPII> installments = installmentNoPIIRepository.getByOrganizationIdAndIudAndStatus(primaryOrg.getOrganizationId(), iud, null);
    if(installments.isEmpty()){
      return Optional.empty();
    }
    if(installments.size() > 1){
      throw new ConflictErrorException("There are too many Instalments having IUD " + iud + " on organizationId " + primaryOrg.getOrganizationId() + ":" +
        getInstallmentDetails(installments)
      );
    }
    InstallmentNoPII installment = installments.getFirst();
    if(!nav.equals(installment.getNav())){
      throw new ConflictErrorException("The found Instalment having IUD " + iud + " on organizationId " + primaryOrg.getOrganizationId() + " has a different NAV:" +
        " expected" + nav + " but found " + installment.getNav()
      );
    }
    return Optional.of(installment);
  }

  private static String getInstallmentDetails(List<InstallmentNoPII> installments) {
    return installments.stream()
      .map(i -> "id: %s, iud: %s, nav: %s, status: %s, syncStatus: %s".formatted(
        i.getInstallmentId(),
        i.getIud(),
        i.getNav(),
        i.getStatus(),
        i.getSyncStatus()))
      .collect(Collectors.joining("\n"));
  }
}
