package it.gov.pagopa.pu.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class RepositoryRestCustomConfigurationTest {

  @Mock
  private PersistentEntities persistentEntities;
  @Mock
  private PersistentEntity<DebtPosition,?> persistentEntity;

  @InjectMocks
  private RepositoryRestCustomConfiguration config;

  @BeforeEach
  void setUp(){
    Mockito.when(persistentEntities.get()).thenReturn(Stream.of(persistentEntity));
    Mockito.when(persistentEntity.getType()).thenReturn(DebtPosition.class);
  }

  @Test
  void givenRepositoryRestConfigurerThenOk() {
    RepositoryRestConfiguration repositoryRestConfiguration = Mockito.mock(RepositoryRestConfiguration.class);
    // Act
    RepositoryRestConfigurer configurer = config.repositoryRestConfigurer();
    configurer.configureRepositoryRestConfiguration(repositoryRestConfiguration,null);

    // Assert
    Assertions.assertNotNull(configurer);
    Mockito.verify(persistentEntities).get();
    Mockito.verify(persistentEntity).getType();
  }
}
