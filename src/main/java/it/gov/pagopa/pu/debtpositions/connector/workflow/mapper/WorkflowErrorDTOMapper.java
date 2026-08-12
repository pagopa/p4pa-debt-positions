package it.gov.pagopa.pu.debtpositions.connector.workflow.mapper;

import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.debtpositions.config.rest.PuErrorDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;

public class WorkflowErrorDTOMapper {

  private WorkflowErrorDTOMapper() {
    /* This utility class should not be instantiated */
  }


  public static PuErrorDTO map(WorkflowErrorDTO errorDTO) {
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
