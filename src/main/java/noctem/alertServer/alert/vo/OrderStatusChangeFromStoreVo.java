package noctem.alertServer.alert.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderStatusChangeFromStoreVo {
    private Long userAccountId;
    private Long purchaseId;
    private String orderStatus;
}
