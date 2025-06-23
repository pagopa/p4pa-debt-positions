package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
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
    DebtPosition debtPosition = debtPositionMapper.mapToModel(debtPositionDTO);

    boolean allInstallmentsToBeDeleted = installmentIdsToDelete.size() ==
      debtPositionDTO.getPaymentOptions().stream()
        .mapToLong(po -> po.getInstallments().size())
        .sum();

    if (allInstallmentsToBeDeleted) {
      debtPositionService.delete(debtPosition);
      debtPositionDTO.setPaymentOptions(new ArrayList<>());
    } else {
      List<Long> emptyPoIds = debtPosition.getPaymentOptions().stream()
        .flatMap(po -> {
          Collection<InstallmentNoPII> allInstallments = po.getInstallments();
          List<InstallmentNoPII> installmentsToRemove = allInstallments.stream()
            .filter(inst -> installmentIdsToDelete.contains(inst.getInstallmentId()))
            .toList();

          installmentsToRemove.forEach(installmentPIIRepository::delete);

          if (installmentsToRemove.size() == allInstallments.size()) {
            return Stream.of(po.getPaymentOptionId());
          } else {
            debtPositionDTO.getPaymentOptions().stream()
              .filter(p -> Objects.equals(p.getPaymentOptionIndex(), po.getPaymentOptionIndex()))
              .findFirst()
              .ifPresent(p ->
                p.setInstallments(
                  p.getInstallments().stream()
                    .filter(i -> !installmentIdsToDelete.contains(i.getInstallmentId()))
                    .toList()));
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
