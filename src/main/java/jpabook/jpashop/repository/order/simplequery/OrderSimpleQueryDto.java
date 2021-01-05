package jpabook.jpashop.repository.order.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(Order order) {
        this.orderId = order.getId();
            /*
            여기서 멤버의 이름을 가지고 오면서 LAZY 가 초기화됨
            영속성 컨텍스트가 멤버의 id를 갖고 찾아봐서 없으면 DB 쿼리를 날림
             */
        this.name = order.getMember().getName();
        this.orderDate = order.getOrderDate();
        this.orderStatus = order.getStatus();
            /*
            여기서 멤버의 이름을 가지고 오면서 LAZY 가 초기화됨
            영속성 컨텍스트가 멤버의 id를 갖고 찾아봐서 없으면 DB 쿼리를 날림
             */
        this.address = order.getDelivery().getAddress();
    }

    public OrderSimpleQueryDto(Long id, String name, LocalDateTime orderDate, OrderStatus status, Address address) {
        this.orderId = id;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = status;
        this.address = address;
    }
}
