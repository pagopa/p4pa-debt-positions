package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
public class InstallmentDeletionServiceImpl implements InstallmentDeletionService {
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentPIIRepository installmentPIIRepository;
  private final DebtPositionService debtPositionService;
  private final DebtPositionMapper debtPositionMapper;

  public InstallmentDeletionServiceImpl(PaymentOptionRepository paymentOptionRepository,
                                        InstallmentPIIRepository installmentPIIRepository, DebtPositionService debtPositionService, DebtPositionMapper debtPositionMapper) {
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentPIIRepository = installmentPIIRepository;
    this.debtPositionService = debtPositionService;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  public void deleteDraftInstallments(DebtPositionDTO debtPositionDTO, Set<Long> installmentIdsToDelete) {
    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> modelPair = debtPositionMapper.mapToModel(debtPositionDTO);
    Map<InstallmentNoPII, Installment> modelMap = modelPair.getSecond();

    boolean allInstallmentsToBeDeleted = installmentIdsToDelete.size() ==
      debtPositionDTO.getPaymentOptions().stream()
        .mapToLong(po -> po.getInstallments().size())
        .sum();

    if (allInstallmentsToBeDeleted) {
      debtPositionService.delete(modelPair.getFirst());
    } else {
      List<Long> emptyPoIds = debtPositionDTO.getPaymentOptions().stream()
        .flatMap(po -> {
          List<InstallmentDTO> allInstallments = po.getInstallments();
          List<InstallmentDTO> installmentsToRemove = allInstallments.stream()
            .filter(inst -> installmentIdsToDelete.contains(inst.getInstallmentId()))
            .toList();

          installmentsToRemove.forEach(inst ->
            modelMap.entrySet().stream()
              .filter(e -> e.getValue().getInstallmentId().equals(inst.getInstallmentId()))
              .map(Map.Entry::getKey)
              .findFirst()
              .ifPresent(installmentPIIRepository::delete)
          );

          if (installmentsToRemove.size() == allInstallments.size()) {
            return Stream.of(po.getPaymentOptionId());
          } else {
            List<InstallmentDTO> keptInstallments = allInstallments.stream()
              .filter(inst -> !installmentIdsToDelete.contains(inst.getInstallmentId()))
              .toList();
            po.setInstallments(keptInstallments);
            return Stream.empty();
          }
        })
        .toList();

      if (!emptyPoIds.isEmpty()) {
        debtPositionDTO.getPaymentOptions().removeIf(po -> emptyPoIds.contains(po.getPaymentOptionId()));
        emptyPoIds.forEach(paymentOptionRepository::deleteById);
        log.debug("Deleted empty PaymentOptions with ids {}", emptyPoIds);
      }
    }
  }

}
