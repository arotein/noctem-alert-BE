package noctem.alertServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    public static ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
