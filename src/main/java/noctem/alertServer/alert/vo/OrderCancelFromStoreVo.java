package noctem.alertServer.alert.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCancelFromStoreVo {
    private Long storeId;
    private Long userAccountId;
    private Integer orderNumber;
    private String orderStatus;
}
