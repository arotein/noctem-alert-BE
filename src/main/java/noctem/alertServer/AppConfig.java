package noctem.alertServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
//    @Bean
//    public SenderOptions senderOptions() {
//        Map<String, Object> producerProps = new HashMap<>();
//        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "http://121.145.206.143:9092");
//        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        return SenderOptions.<String, String>create(producerProps)
//                .maxInFlight(1024);
//    }
//
//    @Bean
//    public Map<String, Object> consumerProps() {
//        Map<String, Object> consumerProps = new HashMap<>();
//        consumerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "http://121.145.206.143:9092");
//        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "${random.uuid}");
//        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        return consumerProps;
//    }
//
//    @Bean
//    public ReceiverOptions receiverOptions() {
//        return ReceiverOptions.<String, String>create(consumerProps())
//                .subscription(Collections.singleton("purchase-to-store"));
//    }
//
//    @Bean
//    public ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate() {
//        return new ReactiveKafkaConsumerTemplate<String, String>(receiverOptions());
//    }

    public static ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setExposedHeaders(Arrays.asList("*"));
//        config.setAllowCredentials(true);
////        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "https://noctem-user1-fe.vercel.app"));
//        config.setAllowedOriginPatterns(Arrays.asList("*"));
//        config.setAllowedHeaders(Arrays.asList("*"));
//        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
//        source.registerCorsConfiguration("/sse/**", config);
//        return source;
//    }
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        return new CorsWebFilter(corsConfigurationSource());
//    }
}
