package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;

@ExtendWith(MockitoExtension.class)
class TransferMapperTest {

  private TransferMapper transferMapper;

  @BeforeEach
  void setUp(){
    transferMapper = new TransferMapper();
  }

  @Test
  void givenValidTransferDTO_WhenMapToModel_ThenReturnTransfer() {
    TransferDTO transferDTO = buildTransferDTO();
    Transfer transferExpected =  buildTransfer();

    Transfer result = transferMapper.mapToModel(transferDTO);

    reflectionEqualsByName(transferExpected, result);
    checkNotNullFields(result, "updateOperatorExternalId", "creationDate", "updateDate");
  }

  @Test
  void givenMapToDtoThenOk(){
    TransferDTO transferExpected = buildTransferDTO();

    TransferDTO result = transferMapper.mapToDto(buildTransfer());

    reflectionEqualsByName(transferExpected, result);
    checkNotNullFields(result, "updateOperatorExternalId", "creationDate", "updateDate");

  }
}
