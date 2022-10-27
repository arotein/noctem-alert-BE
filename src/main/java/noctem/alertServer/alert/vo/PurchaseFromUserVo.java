package noctem.alertServer.alert.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PurchaseFromUserVo {
    private Long storeId;
    private Integer totalMenuQty;
}
