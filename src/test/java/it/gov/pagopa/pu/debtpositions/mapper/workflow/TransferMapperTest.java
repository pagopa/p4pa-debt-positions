package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.workflowhub.dto.generated.TransferRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TransferMapperTest {

  @InjectMocks
  private final TransferRequestMapper mapper = Mappers.getMapper(TransferRequestMapper.class);

  @Test
  void givenMapThenSuccess() {
    TransferRequestDTO expected = buildTransferRequestDTO();

    TransferRequestDTO result = mapper.map(buildTransferDTO());

    checkNotNullFields(result);
    assertEquals(expected, result);
  }
}
