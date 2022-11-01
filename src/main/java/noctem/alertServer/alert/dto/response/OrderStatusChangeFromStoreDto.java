package noctem.alertServer.alert.dto.response;

import lombok.Data;

@Data
public class OrderStatusChangeFromStoreDto {
    private Long userAccountId;
    private Long purchaseId;
    private String orderStatus;

    public OrderStatusChangeFromStoreDto(Long userAccountId, Long purchaseId, String orderStatus) {
        this.userAccountId = userAccountId;
        this.orderStatus = orderStatus;
        this.purchaseId = purchaseId;
    }
}
