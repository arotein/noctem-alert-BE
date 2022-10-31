package noctem.alertServer.alert.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class OrderCancelFromUserResDto {
    private Integer orderNumber;
    private String dateTime;

    public OrderCancelFromUserResDto(Integer orderNumber) {
        this.orderNumber = orderNumber;
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    }
}
