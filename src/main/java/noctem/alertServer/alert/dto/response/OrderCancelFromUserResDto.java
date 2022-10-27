package noctem.alertServer.alert.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class OrderCancelFromUserResDto {
    private Long purchaseId;
    private String orderStatus;
    private String dateTime;

    public OrderCancelFromUserResDto(Long purchaseId) {
        this.purchaseId = purchaseId;
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    }
}
