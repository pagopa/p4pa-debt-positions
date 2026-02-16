package it.gov.pagopa.pu.debtpositions.repository.pii;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.pii.ReceiptPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptNoPIIRepository;
import org.springframework.stereotype.Service;

@Service
public class ReceiptPIIRepositoryImpl extends BasePIIRepository<ReceiptDTO, ReceiptNoPII, ReceiptPIIDTO, Long> implements ReceiptPIIRepository {

  private final ReceiptPIIMapper receiptPIIMapper;
  private final ReceiptNoPIIRepository receiptNoPIIRepository;

  public ReceiptPIIRepositoryImpl(ReceiptPIIMapper receiptPIIMapper, PersonalDataService personalDataService, ReceiptNoPIIRepository receiptNoPIIRepository) {
    super(receiptPIIMapper, personalDataService, receiptNoPIIRepository);
    this.receiptPIIMapper = receiptPIIMapper;
    this.receiptNoPIIRepository = receiptNoPIIRepository;
  }

  @Override
  void setId(ReceiptDTO fullDTO, Long id) {
    fullDTO.setReceiptId(id);
  }

  @Override
  void setId(ReceiptNoPII noPii, Long id) {
    noPii.setReceiptId(id);
  }

  @Override
  Long getId(ReceiptNoPII noPii) {
    return noPii.getReceiptId();
  }

  @Override
  Class<ReceiptPIIDTO> getPIITDTOClass() {
    return ReceiptPIIDTO.class;
  }

  @Override
  PersonalDataType getPIIPersonalDataType() {
    return PersonalDataType.RECEIPT;
  }

  @Override
  public ReceiptDTO findById(Long receiptId) {
    ReceiptNoPII receiptNoPII = receiptNoPIIRepository.findById(receiptId)
      .orElseThrow(() -> new NotFoundException(
        "ReceiptNoPII having receiptId %d not found".formatted(
          receiptId)));
    return receiptPIIMapper.map(receiptNoPII);
  }
}
