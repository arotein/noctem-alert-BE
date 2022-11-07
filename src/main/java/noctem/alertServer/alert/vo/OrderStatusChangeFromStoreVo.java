package noctem.alertServer.alert.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class OrderStatusChangeFromStoreVo {
    private Long storeId;
    private Long userAccountId;
    private Long purchaseId;
    private Integer orderNumber;
    private String orderStatus;
}
