package it.gov.pagopa.pu.debtpositions.mapper.pii.view;

import it.gov.pagopa.pu.common.pii.mapper.BasePIIMapperTest;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.view.InstallmentViewDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallmentViewDTOMapperTest extends BasePIIMapperTest<InstallmentViewDTO, InstallmentViewNoPII, InstallmentPIIDTO> {

  private InstallmentViewDTOMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new InstallmentViewDTOMapper(personalDataServiceMock);
  }

  @Override
  public InstallmentViewDTOMapper getMapper() {
    return mapper;
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
    assertEquals(pii.getOriginalRemittanceInformation(), result.getOriginalRemittanceInformation());
    TestUtils.checkNotNullFields(result);
  }
}
