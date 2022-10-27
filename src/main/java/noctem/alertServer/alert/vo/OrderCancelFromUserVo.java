package noctem.alertServer.alert.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderCancelFromUserVo {
    private Long storeId;
    private Long purchaseId;
}
