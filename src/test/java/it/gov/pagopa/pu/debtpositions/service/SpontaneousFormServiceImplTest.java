package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
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
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  private SpontaneousFormService spontaneousFormService;

  @BeforeEach
  void setUp() {
    spontaneousFormService = new SpontaneousFormServiceImpl(
        spontaneousFormRepositoryMock,
        debtPositionTypeOrgRepositoryMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
        spontaneousFormRepositoryMock,
        debtPositionTypeOrgRepositoryMock);
  }

  @Test
  void whenCreateSpontaneousFormThenOk() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    spontaneousForm.setSpontaneousFormId(null);

    Mockito.when(spontaneousFormRepositoryMock.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(),spontaneousForm.getCode())).thenReturn(Optional.empty());
    Mockito.when(spontaneousFormRepositoryMock.save(spontaneousForm)).thenReturn(spontaneousForm);

    SpontaneousForm response = spontaneousFormService.createSpontaneousForm(spontaneousForm);

    Assertions.assertNotNull(response);
    Assertions.assertEquals(spontaneousForm, response);
  }

  @Test
  void givenExistingSpontaneousFormWithMatchingOrganizationIdAndCodeWhenCreateSpontaneousFormThenConflictErrorException() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    spontaneousForm.setSpontaneousFormId(null);

    Mockito.when(spontaneousFormRepositoryMock.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(),spontaneousForm.getCode())).thenReturn(Optional.of(spontaneousForm));

    Assertions.assertThrows(ConflictErrorException.class,()-> spontaneousFormService.createSpontaneousForm(spontaneousForm));
  }

  @Test
  void givenSpontaneousFormIdNotNullWhenCreateSpontaneousFormThenValidationException() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);

    Assertions.assertThrows(ValidationException.class,()-> spontaneousFormService.createSpontaneousForm(spontaneousForm));

    Mockito.verifyNoInteractions(spontaneousFormRepositoryMock);
  }

  @Test
  void whenDeleteSpontaneousFormThenOk() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    Long spontaneousFormId = spontaneousForm.getSpontaneousFormId();

    Mockito.when(spontaneousFormRepositoryMock.findById(spontaneousFormId)).thenReturn(Optional.of(spontaneousForm));
    Mockito.when(debtPositionTypeOrgRepositoryMock.countBySpontaneousFormId(spontaneousFormId)).thenReturn(0L);
    Mockito.doNothing().when(spontaneousFormRepositoryMock).delete(spontaneousForm);

    Assertions.assertDoesNotThrow(()->spontaneousFormService.deleteSpontaneousForm(spontaneousFormId));
  }

  @Test
  void givenReferencedSpontaneousFormWhenDeleteSpontaneousFormThenConflictErrorException() {
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    Long spontaneousFormId = spontaneousForm.getSpontaneousFormId();

    Mockito.when(spontaneousFormRepositoryMock.findById(spontaneousFormId)).thenReturn(Optional.of(spontaneousForm));
    Mockito.when(debtPositionTypeOrgRepositoryMock.countBySpontaneousFormId(spontaneousFormId)).thenReturn(1L);

    Assertions.assertThrows(ConflictErrorException.class, ()->spontaneousFormService.deleteSpontaneousForm(spontaneousFormId));
  }

  @Test
  void givenNoSpontaneousFormWhenDeleteSpontaneousFormThenNotFoundException() {
    Long spontaneousFormId = 1L;

    Mockito.when(spontaneousFormRepositoryMock.findById(spontaneousFormId)).thenReturn(Optional.empty());

    Assertions.assertThrows(NotFoundException.class, ()->spontaneousFormService.deleteSpontaneousForm(spontaneousFormId));

    Mockito.verifyNoInteractions(debtPositionTypeOrgRepositoryMock);
  }
}
