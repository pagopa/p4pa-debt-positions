package it.gov.pagopa.pu.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.model.BaseEntity;
import it.gov.pagopa.pu.debtpositions.util.UtilitiesTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseEntityListenerTest {

  @Mock
  private BaseEntity baseEntityMock;

  private final BaseEntityListener listener = new BaseEntityListener();

  private final String traceId = "traceId";

  @BeforeEach
  void init(){
    UtilitiesTest.setTraceId(traceId);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(baseEntityMock);
    UtilitiesTest.clearTraceIdContext();
  }

  @Test
  void testOnPrePersist(){
    listener.onPrePersist(baseEntityMock);

    Mockito.verify(baseEntityMock).setUpdateTraceId(traceId);
  }

  @Test
  void testOnPreUpdate(){
    listener.onPreUpdate(baseEntityMock);

    Mockito.verify(baseEntityMock).setUpdateTraceId(traceId);
  }
}
