package noctem.alertServer.Alert.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import noctem.alertServer.Alert.dto.PurchaseResultVo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AlertController {
    private final String PURCHASE_TOPIC = "purchase-to-store";
    private final ObjectMapper objectMapper;
    private final HashMap<Long, Sinks.Many<String>> sinksMap = new HashMap<>();

    @GetMapping(path = "/test/{storeId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamEvent(@PathVariable Long storeId) {
        Sinks.Many<String> sink;
        if (sinksMap.get(storeId) != null) {
            sink = sinksMap.get(storeId);
        } else {
            sink = Sinks.many().multicast().onBackpressureBuffer();
            sinksMap.put(storeId, sink);
        }
        return sink.asFlux();
    }

    @KafkaListener(topics = {PURCHASE_TOPIC})
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        try {
            PurchaseResultVo purchaseResultVo = objectMapper.readValue(consumerRecord.value(), PurchaseResultVo.class);
            sinksMap.get(purchaseResultVo.getStoreId())
                    .tryEmitNext(purchaseResultVo.getPurchaseId().toString());
        } catch (JsonProcessingException ex) {
            log.info("JsonProcessingException");
            throw new RuntimeException(ex);
        }
    }
}
