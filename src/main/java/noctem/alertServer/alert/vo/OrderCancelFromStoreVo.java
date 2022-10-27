package noctem.alertServer.alert.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderCancelFromStoreVo {
    private Long userAccountId;
    private Long purchaseId;
    private String orderStatus;
}
