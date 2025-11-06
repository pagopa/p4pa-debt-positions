package it.gov.pagopa.pu.debtpositions.service.create.debtposition.mixed;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.mapper.TechnicalMixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceResolverService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TechnicalMixedDebtPositionBuilderServiceTest {

  @Mock
  private BalanceResolverService balanceResolverServiceMock;
  @Mock
  private DebtPositionProcessorService debtPositionProcessorService;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private TechnicalMixedDebtPositionBuilderService technicalMixedDebtPositionBuilderService;

  @BeforeEach
  void init() {
    TechnicalMixedDebtPositionMapper technicalMixedDebtPositionMapper = new TechnicalMixedDebtPositionMapper(
      balanceResolverServiceMock, debtPositionTypeOrgRepositoryMock, debtPositionProcessorService);
    technicalMixedDebtPositionBuilderService = new TechnicalMixedDebtPositionBuilderService(
      technicalMixedDebtPositionMapper);
  }

  @Test
  void whenCreateTechnicalMixedDebtPositionsThenOk() {
    DebtPosition debtPosition = buildMixedDebtPosition();
    String accessToken = "TOKEN";

    Long dpTypeOrgId1 = 1L;
    Long dpTypeOrgId2 = 2L;
    List<MixedDpAdditionalData> mixedDpAdditionalDataList1 = List.of(
      MixedDpAdditionalData.builder()
        .transferIndex(1).iud("IUD").balance("balance")
        .legacyPaymentMetadata("legacyPaymentMetadata").build());
    List<MixedDpAdditionalData> mixedDpAdditionalDataList2 = IntStream.range(0,
        2).mapToObj(i -> MixedDpAdditionalData.builder()
        .transferIndex(i + 1).iud("IUD" + i).balance("balance")
        .legacyPaymentMetadata("legacyPaymentMetadata").build())
      .collect(Collectors.toList());

    Map<Long, List<MixedDpAdditionalData>> debtPositionTypeOrgId2TransfersData = new HashMap<>();
    debtPositionTypeOrgId2TransfersData.put(dpTypeOrgId1,
      mixedDpAdditionalDataList1);
    debtPositionTypeOrgId2TransfersData.put(dpTypeOrgId2,
      mixedDpAdditionalDataList2);

    Mockito.doNothing().when(debtPositionProcessorService).updateAmounts(Mockito.any());

    List<DebtPosition> result = technicalMixedDebtPositionBuilderService.createTechnicalMixedDebtPositions(
      debtPositionTypeOrgId2TransfersData, debtPosition, accessToken);

    assertEquals(debtPositionTypeOrgId2TransfersData.size(),
      result.size());
    checkTotalTransfersSize(result,
      (mixedDpAdditionalDataList1.size() + mixedDpAdditionalDataList2.size()));

    checkListsSizesByDebtPositionTypeOrgId(result, dpTypeOrgId1,
      mixedDpAdditionalDataList1);
    checkListsSizesByDebtPositionTypeOrgId(result, dpTypeOrgId2,
      mixedDpAdditionalDataList2);
  }

  private static void checkListsSizesByDebtPositionTypeOrgId(
    List<DebtPosition> result,
    Long dpTypeOrgId, List<MixedDpAdditionalData> mixedDpAdditionalDataList) {
    List<DebtPosition> debtPositionList = result.stream()
      .filter(dp -> dpTypeOrgId.equals(dp.getDebtPositionTypeOrgId())).toList();
    assertEquals(1, debtPositionList.size());

    DebtPosition debtPosition1 = debtPositionList.getFirst();
    assertEquals(1, debtPosition1.getPaymentOptions().size());
    assertEquals(
      mixedDpAdditionalDataList.size(),
      debtPosition1.getPaymentOptions().getFirst().getInstallments().size());
    assertEquals(1,
      debtPosition1.getPaymentOptions().getFirst().getInstallments().getFirst()
        .getTransfers().size());
  }

  private static void checkTotalTransfersSize(
    List<DebtPosition> debtPositionList, int expectedSize) {
    long totalTransfers = debtPositionList.stream()
      .flatMap(debtPosition -> debtPosition.getPaymentOptions().stream())
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .mapToLong(installment -> installment.getTransfers().size())
      .sum();

    assertEquals(expectedSize, totalTransfers);
  }
}
