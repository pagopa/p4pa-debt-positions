package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.InstallmentDetailPIIViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentDetailNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class InstallmentDetailPIIViewRepositoryImplTest {

  @Mock
  private InstallmentDetailNoPIIViewRepository installmentDetailNoPIIViewRepositoryMock;
  @Mock
  private InstallmentDetailPIIViewMapper installmentDetailPIIViewMapperMock;

  private InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository;
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    installmentDetailPIIViewRepository = new InstallmentDetailPIIViewRepositoryImpl(
      installmentDetailNoPIIViewRepositoryMock, installmentDetailPIIViewMapperMock);
  }

  @Test
  void givenExistingInstallmentWhenGetInstallmentDetailThenOk() {
    Long installmentId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";
    InstallmentDetailNoPIIView installmentDetailNoPIIView = podamFactory.manufacturePojo(
      InstallmentDetailNoPIIView.class);
    InstallmentDetailDTO installmentDetail = podamFactory.manufacturePojo(InstallmentDetailDTO.class);

    Mockito.when(installmentDetailNoPIIViewRepositoryMock.findInstallmentDetailView(installmentId, operatorExternalUserId)).thenReturn(
      Optional.of(installmentDetailNoPIIView));
    Mockito.when(installmentDetailPIIViewMapperMock.map(installmentDetailNoPIIView)).thenReturn(installmentDetail);

    InstallmentDetailDTO result = installmentDetailPIIViewRepository.getInstallmentDetail(installmentId, operatorExternalUserId);

    Assertions.assertEquals(installmentDetail, result);
    Mockito.verify(installmentDetailNoPIIViewRepositoryMock).findInstallmentDetailView(installmentId, operatorExternalUserId);
    Mockito.verify(installmentDetailPIIViewMapperMock).map(installmentDetailNoPIIView);
  }

  @Test
  void givenNonExistingInstallmentWhenFindInstallmentThenNotFoundException() {
    Long installmentId = 1L;
    String operatorExternalUserId = "operatorExternalUserId";

    Mockito.when(installmentDetailNoPIIViewRepositoryMock.findInstallmentDetailView(installmentId, operatorExternalUserId))
      .thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class, () -> installmentDetailPIIViewRepository.getInstallmentDetail(installmentId, operatorExternalUserId));

    Mockito.verify(installmentDetailNoPIIViewRepositoryMock).findInstallmentDetailView(installmentId, operatorExternalUserId);
    Mockito.verifyNoInteractions(installmentDetailPIIViewMapperMock);
  }
}
