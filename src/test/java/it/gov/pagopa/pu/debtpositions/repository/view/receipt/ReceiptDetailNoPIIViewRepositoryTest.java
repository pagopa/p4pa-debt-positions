package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import it.gov.pagopa.pu.debtpositions.mapper.MixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReceiptDetailNoPIIViewRepositoryTest {

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  ReceiptDetailNoPIIViewRepository repository;

  @Test
  void testFindReceiptDetailView_emptyList() {
    Mockito.doReturn(Collections.emptyList())
      .when(repository)
      .findReceiptDetailViewInner(1L, "op1", 10L, null);

    Optional<ReceiptDetailNoPIIView> result =
      repository.findReceiptDetailView(1L, "op1", 10L, null);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testFindReceiptDetailView_singleElement() {
    ReceiptDetailNoPIIView view = new ReceiptDetailNoPIIView();

    Mockito.doReturn(List.of(view))
      .when(repository)
      .findReceiptDetailViewInner(1L, "op1", 10L, null);

    Optional<ReceiptDetailNoPIIView> result =
      repository.findReceiptDetailView(1L, "op1", 10L, null);

    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(view, result.get());
  }

  @Test
  void testFindReceiptDetailView_multipleSameDpTypes() {
    ReceiptDetailNoPIIView v1 = new ReceiptDetailNoPIIView();
    v1.setDebtPositionTypeOrgDescription("Tipo A");
    ReceiptDetailNoPIIView v2 = new ReceiptDetailNoPIIView();
    v2.setDebtPositionTypeOrgDescription("Tipo A");

    Mockito.doReturn(List.of(v1, v2))
      .when(repository)
      .findReceiptDetailViewInner(1L, "op1", 10L, null);

    Optional<ReceiptDetailNoPIIView> result =
      repository.findReceiptDetailView(1L, "op1", 10L, null);

    ReceiptDetailNoPIIView first = result.get();

    Assertions.assertEquals(
      MixedDebtPositionMapper.MULTIPLE_REMITTANCE_INFO,
      first.getRemittanceInformation()
    );
    Assertions.assertEquals("Tipo A", first.getDebtPositionTypeOrgDescription());
  }

  @Test
  void testFindReceiptDetailView_multipleDifferentDpTypes() {
    ReceiptDetailNoPIIView v1 = new ReceiptDetailNoPIIView();
    v1.setDebtPositionTypeOrgDescription("Tipo A");
    ReceiptDetailNoPIIView v2 = new ReceiptDetailNoPIIView();
    v2.setDebtPositionTypeOrgDescription("Tipo B");

    Mockito.doReturn(List.of(v1, v2))
      .when(repository)
      .findReceiptDetailViewInner(1L, "op1", 10L, null);

    Optional<ReceiptDetailNoPIIView> result =
      repository.findReceiptDetailView(1L, "op1", 10L, null);

    ReceiptDetailNoPIIView first = result.get();

    Assertions.assertEquals(
      MixedDebtPositionMapper.MULTIPLE_REMITTANCE_INFO,
      first.getRemittanceInformation()
    );
    Assertions.assertEquals("Tipologie multiple", first.getDebtPositionTypeOrgDescription());
  }
}


