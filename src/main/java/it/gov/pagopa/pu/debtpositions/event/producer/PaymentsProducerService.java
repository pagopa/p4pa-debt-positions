package it.gov.pagopa.pu.debtpositions.event.producer;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.dto.PaymentEventDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.enums.PaymentEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

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
        public Supplier<Message<Object>> paymentsProducer() {
            return () -> null;
        }
    }

    public void notifyPaymentsEvent(DebtPositionDTO debtPosition, PaymentEventType event){
        streamBridge.send("paymentsProducer-out-0", binder,
          MessageBuilder.withPayload(new PaymentEventDTO(debtPosition, event))
            .setHeader(KafkaHeaders.KEY, debtPosition.getOrganizationId())
            .build()
        );
    }
}
