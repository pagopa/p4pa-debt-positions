package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class exposing methods related to IUV handling.
 */
@Service
@Slf4j
public class IuvServiceImpl implements IuvService {

  private static final int CHECK_DIGIT_BASE = 93;
  private static final int IUV_LENGTH = 17;

  private final String informationSystemId;
  private final String auxDigit;

  private final IuvSequenceNumberService iuvSequenceNumberService;

  public IuvServiceImpl(@Value("${iuv.information-system-id}") String informationSystemId,
                        @Value("${nav.aux-digit}") String auxDigit,
                        IuvSequenceNumberService iuvSequenceNumberService) {
    this.informationSystemId = informationSystemId;
    this.auxDigit = auxDigit;
    this.iuvSequenceNumberService = iuvSequenceNumberService;
  }

  /**
   * Generate a valid and unique IUV given the organization entity.
   *
   * @param org the organization for which to generate the IUV
   * @return the generated IUV
   */
  public String generateIuv(Organization org, String segregationCode) {
    StringBuilder iuvBuilder = new StringBuilder();
    //header
    iuvBuilder.append(segregationCode);
    iuvBuilder.append(informationSystemId);

    //payment index
    String paymentIndex = generatePaymentIndex(org);
    iuvBuilder.append(paymentIndex);

    //check digit
    String checkDigit = generateCheckDigit(iuvBuilder.toString());
    iuvBuilder.append(checkDigit);

    log.debug("generated new IUV[{}] for organization[{}/{}]", iuvBuilder, org.getIpaCode(), org.getOrgFiscalCode());

    return iuvBuilder.toString();
  }

  private String generatePaymentIndex(Organization org) {
    long paymentIndex = iuvSequenceNumberService.getNextIuvSequenceNumber(org.getOrganizationId());
    if (paymentIndex < 1) {
      log.error("invalid payment index returned for org[{}/{}]: {}", org.getIpaCode(), org.getOrgFiscalCode(), paymentIndex);
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_PAYMENT_INDEX, "invalid payment index");
    }
    return StringUtils.leftPad(String.valueOf(paymentIndex), 11, '0');
  }

  private String generateCheckDigit(String paymentIndex) {
    String digitString = auxDigit + paymentIndex;
    long digit = Long.parseLong(digitString);
    long reminder = digit % CHECK_DIGIT_BASE;
    return StringUtils.leftPad(String.valueOf(reminder), 2, '0');
  }

  /**
   * Utility method to generate the NAV (notice number) given the corresponding IUV.
   *
   * @param iuv the IUV for which to generate the NAV
   * @return the generated NAV
   */
  public String iuv2Nav(String iuv) {
    if (isValidIuv(iuv))
      return auxDigit + iuv;
    else
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IUV, "invalid iuv");
  }

  /**
   * Utility method to extract the IUV given the corresponding NAV (notice number).
   *
   * @param nav the NAV for which to extract the IUV
   * @return the extracted IUV
   */
  public String nav2Iuv(String nav) {
    if (isValidNav(nav)) {
      return nav.substring(auxDigit.length());
    } else {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_NAV, "invalid nav");
    }
  }

  /**
   * Utility method to formally validate a IUV.
   *
   * @param iuv the IUV to validate
   * @return true if valid, otherwise false
   */
  public boolean isValidIuv(String iuv) {
    return isValidNav(StringUtils.join(auxDigit, iuv));
  }

  /**
   * Utility method to formally validate a NAV.
   *
   * @param nav the NAV to validate
   * @return true if valid, otherwise false
   */
  public boolean isValidNav(String nav) {
    if (StringUtils.length(nav) == 18 && Strings.CS.startsWith(nav, auxDigit)) {
      try {
        return Long.parseLong(nav.substring(0, 16)) % CHECK_DIGIT_BASE == Long.parseLong(nav.substring(16));
      } catch (Exception e) {
        return false;
      }
    }
    return false;
  }

  public String validateIuvAndRetrieveNav(String iuv, String segregationCode, DebtPositionOrigin origin) {
    if (StringUtils.length(iuv) != IUV_LENGTH) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IUV, "The iuv must be 17 characters long");
    }
    if (!iuv.substring(0, 2).equals(segregationCode)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IUV, "The first two character of iuv must be the same of segregation code of organization");
    }

    if (origin == DebtPositionOrigin.ORDINARY || origin == DebtPositionOrigin.SPONTANEOUS || origin == DebtPositionOrigin.SPONTANEOUS_SIL) {
      if (!iuv.substring(2,4).equals(informationSystemId)) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IUV, "The third and fourth characters must be '" + informationSystemId + "' for the origin: " + origin);
      }
    } else {
      if (iuv.substring(2,4).equals(informationSystemId)) {
        throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IUV, "The third and fourth characters cannot be '" + informationSystemId + "' for the origin: " + origin);
      }
    }

    return auxDigit + iuv;
  }
}
