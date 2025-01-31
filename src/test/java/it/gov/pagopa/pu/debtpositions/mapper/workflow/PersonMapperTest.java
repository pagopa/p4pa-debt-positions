package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.workflowhub.dto.generated.PersonRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

  @InjectMocks
  private final PersonRequestMapper mapper = Mappers.getMapper(PersonRequestMapper.class);

  @Test
  void testMapPersonDTO() {
    PersonRequestDTO expected = buildPersonRequestDTO();

    PersonRequestDTO person = mapper.map(buildPersonDTO());

    checkNotNullFields(person);
    assertEquals(expected, person);
  }
}
