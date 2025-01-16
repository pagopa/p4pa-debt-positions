package it.gov.pagopa.pu.debtpositions.activities.dao;

public interface IuvSequenceNumberDao {

  /**
   *
   * @param ipaCode
   * @return
   */
  long getNextIuvSequenceNumber(String ipaCode);

}
