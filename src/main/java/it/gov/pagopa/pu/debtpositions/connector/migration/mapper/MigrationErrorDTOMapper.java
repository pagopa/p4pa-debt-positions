package it.gov.pagopa.pu.debtpositions.connector.migration.mapper;

import it.gov.pagopa.pu.debtpositions.config.rest.PuErrorDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.migration.dto.generated.ErrorDTO;

public class MigrationErrorDTOMapper {

  private MigrationErrorDTOMapper() {
    /* This utility class should not be instantiated */
  }


  public static PuErrorDTO map(ErrorDTO errorDTO) {
    return new PuErrorDTO(
      errorDTO.getCategory().getValue(),
      errorDTO.getCode(),
      errorDTO.getMessage(),
      errorDTO.getFields() != null
        ? errorDTO.getFields().stream()
        .map(field -> new ErrorFieldDTO(
          field.getField(),
          field.getError(),
          field.getMessage()
        ))
        .toList()
        : null
    );
  }
}
