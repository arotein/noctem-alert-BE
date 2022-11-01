package noctem.alertServer.alert.dto.response;

import lombok.Data;

@Data
public class OrderCancelFromUserResDto {
    private Integer orderNumber;

    public OrderCancelFromUserResDto(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
}
