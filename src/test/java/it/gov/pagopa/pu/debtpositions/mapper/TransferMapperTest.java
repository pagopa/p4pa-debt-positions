package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransferMapperTest {

  private final TransferMapper transferMapper = new TransferMapper();

  @Test
  void givenValidTransferDTO_WhenMapToModel_ThenReturnTransfer() {
    TransferDTO transferDTO = buildTransferDTO();
    Transfer transferExpected =  buildTransfer();

    Transfer result = transferMapper.mapToModel(transferDTO);

    assertEquals(transferExpected, result);
  }
}
