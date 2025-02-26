package it.gov.pagopa.pu.debtpositions.model.view.installment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.pu.debtpositions.dto.FullPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewNoPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewPIIDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaidViewDTO implements FullPIIDTO<InstallmentPaidViewNoPIIDTO, InstallmentPaidViewPIIDTO> {

  @JsonIgnore
  private InstallmentPaidViewNoPIIDTO noPII;
}
