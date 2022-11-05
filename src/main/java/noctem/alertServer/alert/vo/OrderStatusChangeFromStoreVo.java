package noctem.alertServer.alert.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderStatusChangeFromStoreVo {
    private Long storeId;
    private Long userAccountId;
    private Long purchaseId;
    private Integer orderNumber;
    private String orderStatus;
}
