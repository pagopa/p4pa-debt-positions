package it.gov.pagopa.pu.debtpositions.dto.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffsetDateTimeIntervalFilter implements Serializable {

  private OffsetDateTime from;
  private OffsetDateTime to;

}
