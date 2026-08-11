package it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.exception.common.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionDeleteService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.mixed.TechnicalMixedDebtPositionBuilderService;
import it.gov.pagopa.pu.debtpositions.util.Constants;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TechnicalMixedDebtPositionUpdaterService  {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionRepository debtPositionRepository;
  private final TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderService;
  private final DebtPositionService debtPositionService;
  private final DebtPositionDeleteService debtPositionDeleteService;

  @Transactional
  public List<DebtPosition> update(DebtPosition debtPosition, OffsetDateTime paymentDateTime, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPosition.getDebtPositionTypeOrgId()).orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_NOT_FOUND, "DebtPositionTypeOrg with id " + debtPosition.getDebtPositionTypeOrgId() + " not found"));

    if (!Constants.MIXED_DP_TYPE_ORG_CODE.equalsIgnoreCase(debtPositionTypeOrg.getCode())) {
      return List.of();
    }

    if (debtPosition.getPaymentOptions().size() != 1) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_TOO_MANY_PAYMENT_OPTIONS, "PaymentOptions size must be 1 for debtPositionId " + debtPosition.getDebtPositionId());
    }

    if (debtPosition.getPaymentOptions().getFirst().getInstallments().size() != 1) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_TOO_MANY_INSTALLMENTS, "Installments size must be 1 for debtPositionId " + debtPosition.getDebtPositionId());
    }

    List<DebtPosition> oldMixedDebtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndInstallmentIuv(
      debtPosition.getOrganizationId(),
      debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getIuv(),
      List.of(DebtPositionOrigin.SPONTANEOUS_MIXED)
    );

    List<DebtPosition> newMixedDebtPositions = technicalMixedDebtPositionBuilderService.createTechnicalMixedDebtPositions(
      buildMixedDpAdditionalDataMap(oldMixedDebtPositions),
      debtPosition,
      false,
      paymentDateTime,
      accessToken
    );

    newMixedDebtPositions.forEach(debtPositionService::saveDebtPosition);

    deleteDebtPositionsWithoutPersonalDataId(oldMixedDebtPositions);

    return newMixedDebtPositions;
  }

  private Map<Long, List<MixedDpAdditionalData>> buildMixedDpAdditionalDataMap(List<DebtPosition> oldMixedTechnicalDebtPositions) {
    return oldMixedTechnicalDebtPositions.stream()
      .collect(Collectors.groupingBy(
        DebtPosition::getDebtPositionTypeOrgId,
        Collectors.flatMapping(
          dp -> dp.getPaymentOptions().getFirst().getInstallments().stream()
            .flatMap(installment ->
              installment.getTransfers().stream().map(transfer ->
                MixedDpAdditionalData.builder()
                  .iud(installment.getIud())
                  .balance(installment.getBalance())
                  .transferIndex(transfer.getTransferIndex())
                  .legacyPaymentMetadata(installment.getLegacyPaymentMetadata())
                  .build())),
          Collectors.toList()
        )
      ));
  }

  private void deleteDebtPositionsWithoutPersonalDataId(List<DebtPosition> debtPositions) {
    for (DebtPosition debtPosition : debtPositions) {
      debtPosition.getPaymentOptions().forEach(paymentOption ->
        paymentOption.getInstallments().forEach(installment ->
          installment.setPersonalDataId(null)
        )
      );
      debtPositionDeleteService.delete(debtPosition);
    }
  }
}
