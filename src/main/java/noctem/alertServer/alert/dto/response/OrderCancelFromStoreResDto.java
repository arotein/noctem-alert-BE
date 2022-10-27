package noctem.alertServer.alert.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class OrderCancelFromStoreResDto {
    private String orderStatus;
    private String dateTime;

    public OrderCancelFromStoreResDto(String orderStatus) {
        this.orderStatus = orderStatus;
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    }
}
