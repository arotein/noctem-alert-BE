package noctem.alertServer.alert.dto.response;

import lombok.Data;

@Data
public class PurchaseFromUserResDto {
    private String orderNumber;

    public PurchaseFromUserResDto(Integer orderNumber) {
        this.orderNumber = String.format("A-%d", orderNumber);
    }
}
