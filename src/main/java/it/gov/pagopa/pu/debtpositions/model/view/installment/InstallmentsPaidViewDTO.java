package it.gov.pagopa.pu.debtpositions.model.view.installment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentsPaidViewDTO {

  private List<InstallmentPaidViewDTO> content;

  private Long size;

  private Long totalElements;

  private Long totalPages;

  private Long number;

}
