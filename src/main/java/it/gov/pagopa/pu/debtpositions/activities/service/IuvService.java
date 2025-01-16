package it.gov.pagopa.pu.debtpositions.activities.service;

import it.gov.pagopa.pu.debtpositions.activities.dao.IuvSequenceNumberDao;
import it.gov.pagopa.pu.debtpositions.activities.exception.InvalidValueException;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Service class exposing methods related to IUV handling.
 */
@Lazy
@Service
@Slf4j
public class IuvService {

  public static final String AUX_DIGIT = "3";
  private static final int CHECK_DIGIT_BASE = 93;

  private final String informationSystemId;

  private final IuvSequenceNumberDao iuvSequenceNumberDao;

  public IuvService(@Value("${iuv.informationSystemId:00}") String informationSystemId,
                    IuvSequenceNumberDao iuvSequenceNumberDao) {
    this.informationSystemId = informationSystemId;
    this.iuvSequenceNumberDao = iuvSequenceNumberDao;
  }


  /**
   * Generate a valid and unique IUV given the organization entity.
   * @param org the organization for which to generate the IUV
   * @return the generated IUV
   */
  public String generateIuv(Organization org){
    StringBuilder iuvBuilder = new StringBuilder();
    //header
    iuvBuilder.append(org.getSegregationCode());
    iuvBuilder.append(informationSystemId);

    //payment index
    String paymentIndex = generatePaymentIndex(org);
    iuvBuilder.append(paymentIndex);

    //check digit
    String checkDigit = generateCheckDigit(iuvBuilder.toString());
    iuvBuilder.append(checkDigit);

    return iuvBuilder.toString();
  }

  private String generatePaymentIndex(Organization org){
    long paymentIndex = iuvSequenceNumberDao.getNextIuvSequenceNumber(org.getIpaCode());
    if(paymentIndex<1){
      log.error("invalid payment index returned for org[{}/{}]: {}", org.getIpaCode(), org.getOrgFiscalCode(), paymentIndex);
      throw new InvalidValueException("invalid payment index");
    }
    return StringUtils.leftPad(String.valueOf(paymentIndex), 11, '0');
  }

  private String generateCheckDigit(String paymentIndex){
    String digitString = AUX_DIGIT + paymentIndex;
    long digit = Long.parseLong(digitString);
    long reminder = digit % CHECK_DIGIT_BASE;
    return StringUtils.leftPad(String.valueOf(reminder), 2, '0');
  }

  /**
   * Utility method to generate the NAV (notice number) given the corresponding IUV.
   * @param iuv the IUV for which to generate the NAV
   * @return the generated NAV
   */
  public String iuv2Nav(String iuv){
    if(isValidIuv(iuv))
      return AUX_DIGIT + iuv;
    else
      throw new InvalidValueException("invalid iuv");
  }

  /**
   * Utility method to extract the IUV given the corresponding NAV (notice number).
   * @param nav the NAV for which to extract the IUV
   * @return the extraxted IUV
   */
  public String nav2Iuv(String nav){
    if(isValidNav(nav)){
      return nav.substring(AUX_DIGIT.length());
    } else {
      throw new InvalidValueException("invalid nav");
    }
  }

  /**
   * Utility method to formally validate a IUV.
   * @param iuv the IUV to validate
   * @return true if valid, otherwise false
   */
  public boolean isValidIuv(String iuv){
    return isValidNav(StringUtils.join(AUX_DIGIT,iuv));
  }

  /**
   * Utility method to formally validate a NAV.
   * @param nav the NAV to validate
   * @return true if valid, otherwise false
   */
  public boolean isValidNav(String nav){
    if(StringUtils.length(nav)==18 && StringUtils.startsWith(nav, AUX_DIGIT)){
      try{
        return Long.parseLong(nav.substring(0,16)) % CHECK_DIGIT_BASE == Long.parseLong(nav.substring(16));
      }catch(Exception e){
        return false;
      }
    }
    return false;
  }
}
