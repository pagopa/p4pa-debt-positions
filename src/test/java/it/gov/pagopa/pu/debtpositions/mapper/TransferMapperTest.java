package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TransferMapperTest {

  private TransferMapper transferMapper;

  @Test
  void givenValidTransferDTO_WhenMapToModel_ThenReturnTransfer() {
    TransferDTO transferDTO = buildTransferDTO();
    Transfer transferExpected =  buildTransfer();

    Transfer result = transferMapper.mapToModel(transferDTO);

    assertEquals(transferExpected, result);
    checkNotNullFields(result, "updateOperatorExternalId", "creationDate", "updateDate");
  }
}
