package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
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
  void setUp() {
    transferMapper = new TransferMapper();
  }

  @Test
  void givenValidTransferDTO_WhenMapToModel_ThenReturnTransfer() {
    TransferDTO transferDTO = buildTransferDTO();
    Transfer transferExpected = buildTransfer();
    transferExpected.setStamp(new Stamp());

    Transfer result = transferMapper.mapToModel(transferDTO);

    reflectionEqualsByName(transferExpected, result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    checkNotNullFields(result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId",
      "stampProvincialResidence",
      "stampHashDocument",
      "stampType");
  }

  @Test
  void givenMapToDtoThenOk() {
    TransferDTO transferExpected = buildTransferDTO();

    TransferDTO result = transferMapper.mapToDto(buildTransfer());

    reflectionEqualsByName(transferExpected, result);
    checkNotNullFields(result,
      "stampProvincialResidence",
      "stampHashDocument",
      "stampType");
  }

  @Test
  void givenMapToDtoWithStampNotNullThenOk() {
    TransferDTO transferExpected = buildTransferDTO()
      .stampType("TYPE")
      .stampHashDocument("HASH")
      .stampProvincialResidence("PR");

    Transfer transfer = buildTransfer();
    transfer.setStamp(new Stamp("TYPE", "HASH", "PR"));

    TransferDTO result = transferMapper.mapToDto(transfer);

    reflectionEqualsByName(transferExpected, result);
    checkNotNullFields(result, "updateOperatorExternalId",
      "creationDate",
      "updateDate");
  }
}
