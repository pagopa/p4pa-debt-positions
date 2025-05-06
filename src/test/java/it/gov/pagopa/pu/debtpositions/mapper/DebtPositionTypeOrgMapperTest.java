package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgMapperTest {
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @InjectMocks
  private DebtPositionTypeOrgMapper debtPositionTypeOrgMapper;

  @Test
  void testMapFromDebtPositionType() {
    //given
    DebtPositionType debtPositionType = podamFactory.manufacturePojo(DebtPositionType.class);

    //when
    DebtPositionTypeOrg response = debtPositionTypeOrgMapper.mapFromDebtPositionType(debtPositionType, 1L);

    //verify
    Assertions.assertNotNull(response);
    TestUtils.checkNotNullFields(response, "debtPositionTypeOrgId", "balance", "iban", "postalIban",
      "postalAccountCode", "holderPostalCc", "orgSector", "xsdDefinitionRef", "amountCents", "externalPaymentUrl",
      "notifyOutcomePushOrgSilServiceId", "amountActualizationOrgSilServiceId", "serviceId", "ioTemplateSubject",
      "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }
}
