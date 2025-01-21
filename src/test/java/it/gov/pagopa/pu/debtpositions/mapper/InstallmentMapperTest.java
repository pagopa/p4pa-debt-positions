package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPerson;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;

@ExtendWith(MockitoExtension.class)
class InstallmentMapperTest {

  @Mock
  private PersonMapper personMapperMock;
  @Mock
  private TransferMapper transferMapperMock;

  private InstallmentMapper installmentMapper;

  @BeforeEach
  void setUp(){
    installmentMapper = new InstallmentMapper(personMapperMock, transferMapperMock);
  }

  @Test
  void givenValidInstallmentDTO_WhenMapToModel_ThenReturnInstallment() {
    Installment installmentExpected = buildInstallmentNoUpdate();
    installmentExpected.setStatus(InstallmentStatus.UNPAID);
    InstallmentDTO installmentDTO = buildInstallmentDTO();

    Mockito.when(personMapperMock.mapToModel(buildPersonDTO())).thenReturn(buildPerson());
    Mockito.when(transferMapperMock.mapToModel(buildTransferDTO())).thenReturn(buildTransfer());

    Installment result = installmentMapper.mapToModel(installmentDTO);

    reflectionEqualsByName(installmentExpected, result);
    checkNotNullFields(result, "updateOperatorExternalId", "noPII");
  }

  @Test
  void givenMapToDtoThenOk(){
    InstallmentDTO installmentExpected = buildInstallmentDTO();
    installmentExpected.setStatus(InstallmentStatus.TO_SYNC);
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    installmentNoPII.setTransfers(new TreeSet<>(List.of(buildTransfer())));

    Mockito.when(transferMapperMock.mapToDto(buildTransfer())).thenReturn(buildTransferDTO());

    InstallmentDTO result = installmentMapper.mapToDto(installmentNoPII);

    reflectionEqualsByName(installmentExpected, result, "debtor");
    checkNotNullFields(result, "debtor");
  }
}

