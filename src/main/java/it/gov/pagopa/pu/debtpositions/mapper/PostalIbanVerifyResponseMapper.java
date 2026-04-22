package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PostalIbanVerifyResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PostalIbanVerifyResponseMapper {

   public PostalIbanVerifyResponse map(List<Long> installmentIds, Set<Long> idsWithNull){
     return PostalIbanVerifyResponse.builder()
       .installmentPostalIbanCheck(buildInstallmentPostalIbanCheck(installmentIds, idsWithNull))
       .build();
   }

  private Map<String, Boolean> buildInstallmentPostalIbanCheck(List<Long> installmentIds, Set<Long> idsWithNull){
    return installmentIds.stream()
      .collect(Collectors.toMap(
        String::valueOf,
        id -> !idsWithNull.contains(id)
      ));
  }
}
