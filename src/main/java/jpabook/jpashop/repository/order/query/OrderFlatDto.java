package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/*
join 을 이용하여 한방 쿼리로 다 만들어 버릴 거임
  -> 기존의 `OrderQueryDto` OrderItemQueryDto`를 복붙해서 시작
 */
@Data
@AllArgsConstructor
public class OrderFlatDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    private String itemName;
    private int orderPrice;
    private int count;
}
