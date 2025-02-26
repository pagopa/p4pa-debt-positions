package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewPIIDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentsPaidView {

  private List<InstallmentPaidViewPIIDTO> content;

  private Long size;

  private Long totalElements;

  private Long totalPages;

  private Long number;

}
