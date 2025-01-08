package it.gov.pagopa.pu.debtpositions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPIIDTO {

    private String uniqueIdentifierType;
    private String uniqueIdentifierCode;
    private String fullName;
    private String address;
    private String civic;
    private String postalCode;
    private String location;
    private String province;
    private String nation;
    private String email;

}
