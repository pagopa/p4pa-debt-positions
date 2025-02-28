package it.gov.pagopa.pu.debtpositions.enums;

import lombok.Getter;

@Getter
public enum SignatureType {

  SIGNATURE_NOT_REQUIRED(0),
  CA_DES(1),
  XA_DES(3),
  ADVANCED_ELECTRONICS(4);

  private final int code;

  SignatureType(int code) {
    this.code = code;
  }

  public static SignatureType fromValue(int value) {
    for (SignatureType b : SignatureType.values()) {
      if (b.code == value) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }

}
