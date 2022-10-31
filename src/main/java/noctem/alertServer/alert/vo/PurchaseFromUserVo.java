package noctem.alertServer.alert.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PurchaseFromUserVo {
    private Long storeId;
    private String menuFullName;
    private Integer totalMenuQty;
    private Integer orderNumber;
}
