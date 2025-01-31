package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.workflowhub.dto.generated.InstallmentRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentRequestDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonRequestDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentMapperTest {

  @Mock
  private TransferRequestMapper transferRequestMapperMock;
  @Mock
  private PersonRequestMapper personRequestMapperMock;

  @InjectMocks
  private final InstallmentRequestMapper mapper = Mappers.getMapper(InstallmentRequestMapper.class);

  @Test
  void testMapInstallmentDTO() {
    InstallmentRequestDTO expected = buildInstallmentRequestDTO();
    Mockito.when(transferRequestMapperMock.map(buildTransferDTO())).thenReturn(buildTransferRequestDTO());

    Mockito.when(personRequestMapperMock.map((buildPersonDTO()))).thenReturn(buildPersonRequestDTO());

    InstallmentRequestDTO installment = mapper.map(buildInstallmentDTO());

    checkNotNullFields(installment);
    assertEquals(expected, installment);
  }
}
