package it.gov.pagopa.pu.debtpositions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileResourceDTO {
  private Resource resource;
  private String fileName;
}
