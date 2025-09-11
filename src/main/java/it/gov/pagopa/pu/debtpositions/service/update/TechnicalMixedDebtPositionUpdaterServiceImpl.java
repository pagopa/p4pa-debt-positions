package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionDeleteService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.TechnicalMixedDebtPositionBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TechnicalMixedDebtPositionUpdaterServiceImpl implements TechnicalMixedDebtPositionUpdaterService {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionRepository debtPositionRepository;
  private final TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderService;
  private final DebtPositionDeleteService debtPositionDeleteService;

  @Override
  public List<DebtPosition> update(DebtPosition debtPosition) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPosition.getDebtPositionTypeOrgId()).orElseThrow(() -> new NotFoundException("DebtPositionTypeOrg with id=" + debtPosition.getDebtPositionTypeOrgId() + " not found"));

    if (!"MIXED".equalsIgnoreCase(debtPositionTypeOrg.getCode())) {
      return List.of();
    }

    if (debtPosition.getPaymentOptions().size() != 1) {
      throw new InvalidValueException("paymentOptions size must be 1 for debtPositionId=" + debtPosition.getDebtPositionId());
    }

    if (debtPosition.getPaymentOptions().getFirst().getInstallments().size() != 1) {
      throw new InvalidValueException("installments size must be 1 for debtPositionId=" + debtPosition.getDebtPositionId());
    }

    List<DebtPosition> oldMixedDebtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndIuvAndDebtPositionOrigin(
      debtPosition.getOrganizationId(),
      debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getIuv(),
      DebtPositionOrigin.SPONTANEOUS
    );

    Map<Long, List<MixedDpAdditionalData>> dpTechnicalMixedMap = new HashMap<>();

    oldMixedDebtPositions.forEach(mixedDebtPosition -> {
      if (!dpTechnicalMixedMap.containsKey(mixedDebtPosition.getDebtPositionTypeOrgId())) {
        dpTechnicalMixedMap.put(mixedDebtPosition.getDebtPositionTypeOrgId(), new ArrayList<>());
      }

      mixedDebtPosition.getPaymentOptions().forEach(paymentOption -> {
        paymentOption.getInstallments().forEach(installment -> {
          installment.getTransfers().forEach(transfer -> {
            dpTechnicalMixedMap.get(mixedDebtPosition.getDebtPositionTypeOrgId()).add(
              MixedDpAdditionalData.builder()
                .iud(installment.getIud())
                .balance(installment.getBalance())
                .transferIndex(transfer.getTransferIndex())
                .legacyPaymentMetadata(installment.getLegacyPaymentMetadata())
                .build()
            );
          });
        });
      });
    });

    List<DebtPosition> newMixedDebtPositions = technicalMixedDebtPositionBuilderService.createTechnicalMixedDebtPositions(dpTechnicalMixedMap, debtPosition);

    for (DebtPosition oldMixedDebtPosition : oldMixedDebtPositions) {
      oldMixedDebtPosition.getPaymentOptions().forEach(paymentOption -> {
        paymentOption.getInstallments().forEach(installment -> {
          installment.setPersonalDataId(null);
        });
      });
      debtPositionDeleteService.delete(oldMixedDebtPosition);
    }

    return newMixedDebtPositions;
  }
}
