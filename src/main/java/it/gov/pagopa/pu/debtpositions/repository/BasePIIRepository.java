package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.FullPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.PIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.BasePIIMapper;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Pair;

import java.io.Serializable;
import java.util.Optional;

public abstract class BasePIIRepository<F extends FullPIIDTO<E, P>, E extends NoPIIEntity<P>, P extends PIIDTO, I extends Serializable> {

  private final BasePIIMapper<F, E, P> piiMapper;
  private final PersonalDataService personalDataService;
  private final JpaRepository<E, I> noPIIRepository;

  BasePIIRepository(BasePIIMapper<F, E, P> piiMapper, PersonalDataService personalDataService, JpaRepository<E, I> noPIIRepository) {
    this.piiMapper = piiMapper;
    this.personalDataService = personalDataService;
    this.noPIIRepository = noPIIRepository;
  }

  abstract void setId(F fullDTO, I id);
  abstract void setId(E noPii, I id);
  abstract I getId(E noPii);
  abstract Class<P> getPIITDTOClass();
  abstract PersonalDataType getPIIPersonalDataType();

  public F save(F fullDTO) {
    Pair<E, P> p = piiMapper.map(fullDTO);

    Pair<Long, Optional<P>> piiId2OldPii = retrievePII(p.getFirst());
    boolean pii2create = piiId2OldPii==null ||
      piiId2OldPii.getSecond().isEmpty() ||
      !p.getSecond().equals(piiId2OldPii.getSecond().get());

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

}
