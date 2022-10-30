package noctem.alertServer.alert.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCancelFromUserVo {
    private Long storeId;
    private Integer orderNumber;
    private Long userAccountId;
}
