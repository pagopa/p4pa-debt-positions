package it.gov.pagopa.pu.debtpositions.dto.filters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalDateTimeIntervalFilter implements Serializable {

  private LocalDateTime from;
  private LocalDateTime to;

}
