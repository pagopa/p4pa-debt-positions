package it.gov.pagopa.pu.debtpositions.event.producer;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.dto.PaymentEventDTO;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class PaymentsProducerService {

  @Value("${spring.cloud.stream.bindings.paymentsProducer-out-0.binder}")
  private String binder;

  private final StreamBridge streamBridge;

  public PaymentsProducerService(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Configuration
  static class PaymentsProducerConfig {
    @Bean
    public Supplier<Message<PaymentEventDTO>> paymentsProducer() {
      return () -> null;
    }
  }

  public void notifyPaymentsEvent(DebtPositionDTO debtPosition, PaymentEventType event, String eventDescription) {
    String eventId = event.name() + debtPosition.getDebtPositionId() + UUID.randomUUID();
    String traceId = Utilities.getTraceId();
    streamBridge.send("paymentsProducer-out-0", binder,
      MessageBuilder.withPayload(new PaymentEventDTO(eventId, traceId, event, OffsetDateTime.now(), debtPosition, eventDescription))
        .setHeader(KafkaHeaders.KEY, String.valueOf(debtPosition.getOrganizationId()))
        .build()
    );
  }
}
