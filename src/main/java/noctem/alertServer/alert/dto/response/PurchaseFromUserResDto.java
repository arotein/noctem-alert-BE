package noctem.alertServer.alert.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class PurchaseFromUserResDto {
    private String orderNumber;
    private String dateTime;

    public PurchaseFromUserResDto(Integer orderNumber) {
        this.orderNumber = String.format("A-%d", orderNumber);
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    }
}
