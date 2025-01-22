package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallmentPIIRepositoryImpl implements InstallmentPIIRepository {

  @PersistenceContext
  private EntityManager entityManager;

    private final InstallmentPIIMapper installmentPIIMapper;
    private final PersonalDataService personalDataService;
    private final InstallmentNoPIIRepository installmentNoPIIRepository;

    public InstallmentPIIRepositoryImpl(InstallmentPIIMapper installmentPIIMapper, PersonalDataService personalDataService, InstallmentNoPIIRepository installmentNoPIIRepository) {
        this.installmentPIIMapper = installmentPIIMapper;
        this.personalDataService = personalDataService;
        this.installmentNoPIIRepository = installmentNoPIIRepository;
    }

    @Override
    public long save(Installment installment) {
        Pair<InstallmentNoPII, InstallmentPIIDTO> p = installmentPIIMapper.map(installment);
        long personalDataId = personalDataService.insert(p.getSecond(), PersonalDataType.INSTALLMENT);
        p.getFirst().setPersonalDataId(personalDataId);
        installment.setNoPII(p.getFirst());
        long newId = installmentNoPIIRepository.save(p.getFirst()).getInstallmentId();
        installment.setInstallmentId(newId);
        installment.getNoPII().setInstallmentId(newId);
        return newId;
    }

  @Override
  public long countExistingDebtPosition(DebtPosition debtPosition) {
    String query = """
    SELECT COUNT(dp)
    FROM DebtPosition dp
    JOIN dp.paymentOptions po
    JOIN po.installments i
    WHERE dp.organizationId = :organizationId
      AND i.status <> 'CANCELLED'
      AND ((i.iud IN :iudList) OR (i.iuv IN :iuvList) OR (i.nav IN :navList))
    """;

    List<String> iudList = debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .map(InstallmentNoPII::getIud)
      .toList();

    List<String> iuvList = debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .map(InstallmentNoPII::getIuv)
      .toList();

    List<String> navList = debtPosition.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .map(InstallmentNoPII::getNav)
      .toList();

    return entityManager.createQuery(query, Long.class)
      .setParameter("organizationId", debtPosition.getOrganizationId())
      .setParameter("iudList", iudList)
      .setParameter("iuvList", iuvList)
      .setParameter("navList", navList)
      .getSingleResult();
  }
}
