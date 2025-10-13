package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.repository.SpontaneousFormRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import jakarta.validation.ValidationException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class SpontaneousFormServiceImplTest {
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  @Mock
  private SpontaneousFormRepository spontaneousFormRepositoryMock;
  private SpontaneousFormService spontaneousFormService;

  @BeforeEach
  void setUp() {
    spontaneousFormService = new SpontaneousFormServiceImpl(
        spontaneousFormRepositoryMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
        spontaneousFormRepositoryMock);
  }

  @Test
  void whenGetReceiptThenOk() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    spontaneousForm.setSpontaneousFormId(null);

    Mockito.when(spontaneousFormRepositoryMock.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(),spontaneousForm.getCode())).thenReturn(Optional.empty());
    Mockito.when(spontaneousFormRepositoryMock.save(spontaneousForm)).thenReturn(spontaneousForm);

    SpontaneousForm response = spontaneousFormService.createSpontaneousForm(spontaneousForm);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(spontaneousForm, response);
  }

  @Test
  void givenExistingSpontaneousFormWithMatchingOrganizationIdAndCodeWhenGetReceiptThenConflictErrorException() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    spontaneousForm.setSpontaneousFormId(null);

    Mockito.when(spontaneousFormRepositoryMock.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(),spontaneousForm.getCode())).thenReturn(Optional.of(spontaneousForm));

    Assertions.assertThrows(ConflictErrorException.class,()-> spontaneousFormService.createSpontaneousForm(spontaneousForm));
  }

  @Test
  void givenSpontaneousFormIdNotNullWhenGetReceiptThenValidationException() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);

    Assertions.assertThrows(ValidationException.class,()-> spontaneousFormService.createSpontaneousForm(spontaneousForm));

    Mockito.verifyNoInteractions(spontaneousFormRepositoryMock);
  }
}
