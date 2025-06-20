package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
@Slf4j
public class InstallmentDeletionServiceImpl implements InstallmentDeletionService {
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final TransferRepository transferRepository;

  public InstallmentDeletionServiceImpl(PaymentOptionRepository paymentOptionRepository,
                                        InstallmentNoPIIRepository installmentNoPIIRepository,
                                        TransferRepository transferRepository) {
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.transferRepository = transferRepository;
  }

  @Override
  public void deleteDraftInstallments(DebtPositionDTO debtPositionDTO, Set<Long> installmentIdsToDelete) {
    List<Long> emptyPoIds = debtPositionDTO.getPaymentOptions().stream()
      .flatMap(po -> {
        List<InstallmentDTO> toRemove = po.getInstallments().stream()
          .filter(inst -> installmentIdsToDelete.contains(inst.getInstallmentId()))
          .toList();

        toRemove.forEach(inst -> {
          inst.getTransfers().forEach(t -> transferRepository.deleteById(Objects.requireNonNull(t.getTransferId())));
          inst.getTransfers().clear();
          installmentNoPIIRepository.deleteById(Objects.requireNonNull(inst.getInstallmentId()));
        });

        po.getInstallments().removeAll(toRemove);

        return po.getInstallments().isEmpty()
          ? Stream.of(po.getPaymentOptionId())
          : Stream.empty();
      })
      .toList();


    if (!emptyPoIds.isEmpty()) {
      debtPositionDTO.getPaymentOptions().removeIf(po -> emptyPoIds.contains(po.getPaymentOptionId()));
      emptyPoIds.forEach(paymentOptionRepository::deleteById);
      log.debug("Deleted empty PaymentOptions with ids {}", emptyPoIds);
    }
  }
}
