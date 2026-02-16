package it.gov.pagopa.pu.common.pii.repository;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.dto.FullEntityPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.common.pii.mapper.BaseEntityPIIMapper;
import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Pair;

import java.io.Serializable;
import java.util.Optional;

/**
 * A repository which will handle the PII segregation.<<BR/>
 * It will split the full representation of the entity ({@link FullEntityPIIDTO} into {@link NoPIIEntity} and {@link PIIDTO}, storing the PII on a separate DB and setting its identifier on the {@link NoPIIEntity}'s personalDataId field.
 */
public abstract class BasePIIRepository<F extends FullEntityPIIDTO<E, P>, E extends NoPIIEntity<P>, P extends PIIDTO, I extends Serializable> {

  private final BaseEntityPIIMapper<F, E, P> piiMapper;
  private final PersonalDataService personalDataService;
  private final JpaRepository<E, I> noPIIRepository;

  protected BasePIIRepository(BaseEntityPIIMapper<F, E, P> piiMapper, PersonalDataService personalDataService, JpaRepository<E, I> noPIIRepository) {
    this.piiMapper = piiMapper;
    this.personalDataService = personalDataService;
    this.noPIIRepository = noPIIRepository;
  }

  protected abstract void setId(F fullDTO, I id);
  protected abstract void setId(E noPii, I id);
  protected abstract I getId(E noPii);
  protected abstract Class<P> getPIITDTOClass();
  protected abstract PersonalDataType getPIIPersonalDataType();

  public F save(F fullDTO) {
    Pair<E, P> p = piiMapper.map(fullDTO);

    Pair<Long, Optional<P>> piiId2OldPii = retrievePII(p.getFirst());
    boolean pii2create = piiId2OldPii==null ||
      piiId2OldPii.getSecond()
        .map(o -> !o.equals(p.getSecond()))
        .orElse(false);

    if (piiId2OldPii != null && pii2create) {
      personalDataService.delete(piiId2OldPii.getFirst());
    }

    long personalDataId;
    if(pii2create) {
      personalDataId = personalDataService.insert(p.getSecond(), getPIIPersonalDataType());
    } else {
      personalDataId = piiId2OldPii.getFirst();
    }
    p.getFirst().setPersonalDataId(personalDataId);

    fullDTO.setNoPII(p.getFirst());
    E savedNoPii = noPIIRepository.save(p.getFirst());
    setId(fullDTO, getId(savedNoPii));
    setId(fullDTO.getNoPII(), getId(savedNoPii));
    return fullDTO;
  }

  protected Pair<Long, Optional<P>> retrievePII(E noPii) {
    Long personalDataId = noPii.getPersonalDataId();
    I id = getId(noPii);
    if(personalDataId==null && id != null){
      personalDataId = noPIIRepository.findById(id).map(NoPIIEntity::getPersonalDataId).orElse(null);
    }

    if (personalDataId != null) {
      return Pair.of(personalDataId, Optional.ofNullable(personalDataService.get(personalDataId, getPIITDTOClass())));
    } else {
      return null;
    }
  }

  public void delete(E noPii) {
    if (noPii.getPersonalDataId() != null) {
      personalDataService.delete(noPii.getPersonalDataId());
    }

    noPIIRepository.delete(noPii);
  }

}
