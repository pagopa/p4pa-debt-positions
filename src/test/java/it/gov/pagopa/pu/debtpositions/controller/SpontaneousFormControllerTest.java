package it.gov.pagopa.pu.debtpositions.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.service.SpontaneousFormService;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class SpontaneousFormControllerTest {

  public static final PodamFactory podamFactory = TestUtils.getPodamFactory();
  @Mock
  private SpontaneousFormService spontaneousFormServiceMock;

  private SpontaneousFormController controller;

  @BeforeEach
  void setUp() {
    controller = new SpontaneousFormController(spontaneousFormServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(spontaneousFormServiceMock);
  }

  @Test
  void whenCreateSpontaneousFormThenReturnCreated() {
    SpontaneousForm expectedResult = podamFactory.manufacturePojo(SpontaneousForm.class);

    when(spontaneousFormServiceMock.createSpontaneousForm(expectedResult))
        .thenReturn(expectedResult);

    ResponseEntity<SpontaneousForm> result = controller.createSpontaneousForm(expectedResult);

    assertNotNull(result);
    assertEquals(HttpStatus.CREATED, result.getStatusCode());
    assertEquals(expectedResult,result.getBody());
  }
}
