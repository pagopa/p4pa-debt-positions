package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;

public class InstallmentSynchronizeFaker {

  public static InstallmentSynchronizeDTO buildInstallmentSynchronizeDTO(){
    return TestUtils.getPodamFactory().manufacturePojo(InstallmentSynchronizeDTO.class);
  }

  public static TransferSynchronizeDTO buildTransferSynchronizeDTO(){
    return TestUtils.getPodamFactory().manufacturePojo(TransferSynchronizeDTO.class);
  }
}
