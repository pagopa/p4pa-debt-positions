package it.gov.pagopa.pu.debtpositions.connector.classification.config;

import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.ValidateBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.BaseApiHolderTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ClassificationApisHolderTest extends BaseApiHolderTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilderMock;

    private ClassificationApisHolder classificationApisHolder;

    @BeforeEach
    void setUp() {
        Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
        Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
        ClassificationApiClientConfig clientConfig = ClassificationApiClientConfig.builder()
          .baseUrl("http://example.com")
          .build();
        classificationApisHolder = new ClassificationApisHolder(clientConfig, restTemplateBuilderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
                restTemplateBuilderMock,
                restTemplateMock
        );
    }

    @Test
    void whenBalanceApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
        assertAuthenticationShouldBeSetInThreadSafeMode(
                accessToken -> classificationApisHolder.getBalanceApi(accessToken)
                        .validateBalance(new ValidateBalanceRequest()),
                new ParameterizedTypeReference<>() {},
                classificationApisHolder::unload);
    }

    @Test
    void whenAssessmentsRegistrySearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
        assertAuthenticationShouldBeSetInThreadSafeMode(
                accessToken -> classificationApisHolder.getAssessmentsRegistrySearchControllerApi(accessToken)
                        .crudAssessmentsRegistriesFindAssessmentsRegistriesByFilters(
                          1L,
                          Set.of("typCode"),
                          null, null, null, null, null, null,
                          "2025",
                          AssessmentsRegistryStatus.ACTIVE,
                          0,
                          1,
                          null
                        ),
                new ParameterizedTypeReference<>() {},
                classificationApisHolder::unload);
    }

}
