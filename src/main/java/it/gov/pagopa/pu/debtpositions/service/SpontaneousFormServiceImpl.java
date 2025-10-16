package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.SpontaneousFormRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SpontaneousFormServiceImpl implements SpontaneousFormService {
  private final SpontaneousFormRepository spontaneousFormRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  public SpontaneousFormServiceImpl(SpontaneousFormRepository spontaneousFormRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    this.spontaneousFormRepository = spontaneousFormRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Transactional
  @Override
  public SpontaneousForm createSpontaneousForm(SpontaneousForm spontaneousForm) {
    if(spontaneousForm.getSpontaneousFormId()!=null){
      throw new ValidationException("SpontaneousFormId must be null");
    }
    Optional<SpontaneousForm> optSpontaneousForm = spontaneousFormRepository.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(), spontaneousForm.getCode());
    if(optSpontaneousForm.isPresent()){
      throw new ConflictErrorException("There is another SpontaneousForm with organizationId "+spontaneousForm.getOrganizationId()+" and code "+spontaneousForm.getCode());
    }
    return spontaneousFormRepository.save(spontaneousForm);
  }

  @Override
  public void deleteSpontaneousForm(Long spontaneousFormId) {
    SpontaneousForm spontaneousForm = spontaneousFormRepository.findById(
        spontaneousFormId).orElseThrow(
        () -> new NotFoundException("SpontaneousForm having id "+spontaneousFormId+" not found"));

    long dptoCount = debtPositionTypeOrgRepository.countBySpontaneousFormId(spontaneousFormId);
    if(dptoCount > 0L){
      throw new ConflictErrorException("The SpontaneousForm having id "+spontaneousFormId+" is referenced by "+dptoCount+" DebtPositionTypeOrgs");
    }
    spontaneousFormRepository.delete(spontaneousForm);
  }
}
