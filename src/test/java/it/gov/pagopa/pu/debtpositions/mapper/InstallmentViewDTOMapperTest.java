package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentViewDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstallmentViewDTOMapperTest {
  @Mock
  private PersonalDataService personalDataServiceMock;

  private InstallmentViewDTOMapper mapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    mapper = new InstallmentViewDTOMapper(personalDataServiceMock);
  }

  @Test
  void testMapper() {
    // given
    InstallmentViewNoPII noPII = InstallmentViewNoPII.builder()
      .installmentId(1L)
      .debtPositionId(12L)
      .paymentOptionId(123L)
      .receiptId(1234L)
      .iuv("IUV123456")
      .iud("IUD123456")
      .status(InstallmentStatus.UNPAID)
      .nav("NAV123456")
      .dueDate(LocalDate.now())
      .amountCents(10000L)
      .remittanceInformation("Test remittance info")
      .personalDataId(1L)
      .debtorFiscalCodeHash("DebtorHash123".getBytes(StandardCharsets.UTF_8))
      .debtPositionTypeOrgDescription("debtPositionTypeOrgDescription")
      .build();
    InstallmentPIIDTO pii = podamFactory.manufacturePojo(InstallmentPIIDTO.class);

    when(personalDataServiceMock.get(noPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(pii);
    // when
    InstallmentViewDTO result = mapper.map(noPII);
    //then
    assertNotNull(result);
    TestUtils.reflectionEqualsByName(noPII, result);
    TestUtils.reflectionEqualsByName(pii.getOriginalRemittanceInformation(), result.getOriginalRemittanceInformation());
    TestUtils.checkNotNullFields(result);
  }
}
