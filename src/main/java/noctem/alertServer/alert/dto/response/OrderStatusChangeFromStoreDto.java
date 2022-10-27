package noctem.alertServer.alert.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class OrderStatusChangeFromStoreDto {
    private Long userAccountId;
    private Long purchaseId;
    private String orderStatus;
    private String dateTime;

    public OrderStatusChangeFromStoreDto(Long userAccountId, Long purchaseId, String orderStatus) {
        this.userAccountId = userAccountId;
        this.orderStatus = orderStatus;
        this.purchaseId = purchaseId;
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
    }
}
