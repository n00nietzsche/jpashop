package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderAPIController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        /*
        Hibernate5Module 을 사용해서, 데이터가 정상적으로 로딩된 것들만 보여지게 된다.
         */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            /*
            Lazy Loading 일 때 Hibernate5Module 은 기본설정이 프록시인 것은 안뿌림
            그런데 이렇게 객체 그래프를 강제 초기화하면 데이터를 뿌림
            이렇게 한번 접근을 해줘야 함 (OrderItem 과 Item 초기화)

            물론 양방향 관계는 꼭 찾아서 `@JsonIgnore`를 넣어줘야 함
            */
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(orderItem -> orderItem.getItem().getName());
        }

        return all;
    }


    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        return orders
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        return orders
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @Data
    /*
    Jackson 에서 TypeDefinition Error (no properties) 날 때는
    `@Data` 혹은 `@Getter` 어노테이션과 같은 Getter 생성이 있는지 확인

    실행결과, orderItems 가 null 로 나옴.
    왜냐하면 OrderItem 은 엔티티임

    이 방법의 문제는 완전히 엔티티에 대한 의존을 끊지 않아서
    엔티티가 노출된다는 것임

    List 의 `<OrderItem>` 조차도 다 DTO 로 변경해야 함

    이렇게 엔티티에 의존하면, 나중에 `OrderItem`이 변경되었을 때,
    API 스펙이 전부 다 바뀌어버림
     */
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            /*
            Lazy Loading 시에 Hibernate5Module 이
            null 로 표기하지 않게 만들기 위해서 한번씩 접근
             */
            // order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            // orderItems = order.getOrderItems();
            orderItems =  order.getOrderItems().stream().map(OrderItemDto::new).collect(Collectors.toList());

        }
    }


    /*
    엔티티를 노출하지 말라는 것은 List 내부에 <> 타입까지 노출하지 말라는 것이다.
    단, Address 와 같은 Value Object 는 노출해도 무방하다.
     */
    @Getter
    static class OrderItemDto {
        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    /*
    결과 쿼리가 상당히 많이 실행된다.
    첫번째 예를 들면,
    Order 조회 1, Member 조회 1, Delivery 조회 1, OrderItem 조회 1, OrderItem 내부 Item 조회 2
    6개가 수행된다.
    그리고 Order 조회 결과 멤버의 수가 2명일 것이기 때문에 최악의 경우 ( 불러놓은 엔티티 중에 일치하는 것이 아무것도 없다면)
    총 11회가 실행될 것이다.
     */
}
