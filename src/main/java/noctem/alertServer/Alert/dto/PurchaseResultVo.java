package noctem.alertServer.Alert.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResultVo {
    private Long storeId;
    private Long purchaseId;
}
