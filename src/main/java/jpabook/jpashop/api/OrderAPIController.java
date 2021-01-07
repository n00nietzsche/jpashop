package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderAPIController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

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
                .collect(toList());
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        return orders
                .stream()
                .map(OrderDto::new)
                .collect(toList());
    }


    /*
        그냥 `findAllWithMemberDelivery` 메소드를 사용하면 이전에 fetch join 을 사용하지 않았을 때처럼
        쿼리가 여러번 날아가는 문 제(N+1 문제)가 발생하게 될 것이다.

        `findAllWithMemberDelivery` 메소드에서는 `Member`와 `Delivery`만 페치 조인하기 때문에
        각 `OrderItem` 과 `OrderItem 의 Item` 을 가져오는 부분이 N+1 로 조회된다.

        `spring.jpa.properties.default_batch_fetch_size: 100`을 `application.yml`에 추가해주면,
        SQL WHERE 절에서 IN 을 통해서 해당하는 `Order.id`를 넣어 `Order.OrderItems`의 `OrderItem`을 가져온다.
        마찬가지로 `OrderItem.item` 에 해당하는 `OrderItem.id` 를 넣어 `Item` 도 조회한다.
        그래서 총 쿼리 3번 (Order, OrderItem, Item)에 데이터를 가져올 수 있다.
        1, N, M 의 쿼리 횟수를 1, 1, 1 로 최적화한다.

        `spring.jpa.properties.default_batch_fetch_size: 100`에서
        `100`이란 숫자는 IN 에 한번에 들어가는 숫자가 얼마나 되냐를 결정한다.
        `10`인데 `50`개의 데이터를 가져와야 한다면 IN 에 숫자를 `10`개씩 5번을 날릴 것이다.

        그냥 전체에 fetch join 을 사용하는 방법(V3)과 비교했을 때,
        이 방법이 실제로 DB 에서 JPA 로 넘어오는 데이터의 양이 더욱 최적화 되어 있다.
        V3는 아무리 distinct 를 하더라도 DB 에서 실제로 JPA 로 넘기는 데이터는
        join 에 의해 부풀려진 row 들이 포함된다.

        하지만 V3.1 의 방법은 비록 쿼리가 더 많이 날아가긴 해도,
        부풀려진 row 들을 전혀 포함하지 않고, 정규화된 row 들로 날아간다.
        이러한 V3 과 V3.1 은 환경에 따라 낼 수 있는 성능이 다를 것이므로
        취사선택 할 수 있다.

        만일 join 전의 테이블의 내용이 아주 많다면,
        1:N 전체 fetch join 방식(V3)은 아주 불리한 성능을 갖게 될 것이고,

        join 전의 테이블 내용이 별로 없다면,
        1:N 전체 fetch join 방식(V3)이 아주 유리한 성능을 갖게 될 것이다.

        사실 이러한 성능 차이를 떠나서,
        1:N 전체 fetch join 방식(V3)은 페이징이 불가능하다는 치명적인 단점이 있으므로
        X:1 관계만 fetch join 을 한 뒤에
        `spring.jpa.properties.default_batch_fetch_size` 속성을 이용한 방법인
        V3.1 방식이 조금 더 낫다고 볼 수 있다.

        V3.1 방식에서 X:1 관계도 fetch join 을 하지 않는다면,
        모든 관계를 다 IN 방식으로 조회하게 된다.

        `spring.jpa.properties.default_batch_fetch_size`를 디테일하게 적용하고 싶다면,
        엔티티 컬럼의 어노테이션에 `@BatchSize`를 적용하거나,
        엔티티 클래스 자체 어노테이션에 `@BatchSize`를 적용하면 된다.

        ## 정리
        - 장점
          - 쿼리 호출 수가 `1 + N` -> `1 : 1`로 최적화된다. (batch_fetch_size 때문)
          - 조인보다 DB 데이터 전송량이 최적화된다.
            (Order와 OrderItem을 조인하면 Order가 OrderItem만큼 중복회서 조회된다.
             이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없다.)
          - 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
          - 컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능하다.
        - 결론
          - ToOne 관계는 페치조인해도 페이징에 영향을 주지 않는다.
          따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄여서 해결하고,
          나머지는 `hibernate.default_batch_fetch_size`로 최적화하자.

        > 참고: `default_batch_fetch_size` 의 크기는 적당한 사이즈를 골라야 하는데,
        100~1000 사이를 선택하는 것을 권장한다. 이 전략은 SQL IN 절을 사용하는데,
        데이터베이스에 따라 IN 절 파라미터를 1000으로 제한하기도 한다.
        1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로
        DB에 순간 부하가 증가할 수도 있다.
        하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다.
        1000으로 설정하는 것이 성능상 가장 좋지만,
        결국 DB든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있는지로 결정하면 된다.

        > 작을수록 Query의 개수가 증가하고, DB의 부하가 줄어들고
          클수록 Query의 개수가 줄어들고, DB의 부하가 증가한다.
          CPU와 메모리 양이 크다면 최대한 늘리는 게 빠를 것이다.
         */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return orders
                .stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    @GetMapping("/api/v4/orders")
    /*
    Intellij에서 F2누르면 다음 에러로 빠르게 갈 수 있음

    완전히 DTO를 이용하면? 총 DTO만 3개 필요
    - Query: 루트 1번, 컬렉션 N번 실행
    - ToOne(N:1, 1:1) 관계들을 먼저 조회하고,ToMany(1:N) 관계는 각각 별도로 처리한다.
      - 이런 방식을 선택한 이유는 다음과 같다.
        - ToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다.
        - ToMany(1:N) 관계는 조인하면 row 수가 증가한다.
    - row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화하기 쉬우므로 한번에 조회하고,
      ToMany 관계는 최적화하기 어려우므로 `findOrderItems()` 같은 별도의 메소드로 조회한다.
    - 조회는 총 N+1 번이 된다. (각 Order에 대한 OrderItems를 별개로 불러온다.)
     */
    public List<OrderQueryDto> ordersV4() {
            return orderQueryRepository.findOrderQueryDtos();
    }


    /*
    ## 정리
    - Query: 루트 1번, 컬렉션 1번
    - ToOne 관계들을 먼저 조회하고, 여기서 얻은 orderId로 ToMany 관계인 `OrderItem`을 한꺼번에 조회
    - Map을 사용해서 매칭 성능 향상 (O(1))
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /*
    `OrderFlatDto`로 하면 중복이 생기는데,
    기존 스펙과 동일하게 출력해주고 싶다면?
      -> 기존 DTO에 맞춰 다 넣어줌

     ## 정리
     - 장점은 쿼리 한번에 다 가져온다는 것 밖에 없다.
     - 쿼리는 한번이지만 조인으로 인해서
     DB에서 애플리케이션에 전달하는
     데이터에 중복 데이터가 추가되므로
     상황에 따라 V5보다 더 느릴 수도 있다.
     - 애플리케이션에서 추가 작업이 크다.
     - 페이징 불가능 (Order를 기준으로는 안되고, OrderItem을 기준으로는 된다.)

     **쿼리가 줄어든다고 항상 좋은 것은 아니다.**
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return   flats.stream()
                .collect(
                        groupingBy((OrderFlatDto o) ->
                                        new OrderQueryDto(
                                                o.getOrderId()
                                                , o.getName()
                                                , o.getOrderDate()
                                                , o.getOrderStatus()
                                                , o.getAddress())
                                , mapping((OrderFlatDto o) ->
                                        new OrderItemQueryDto(
                                                o.getOrderId()
                                                , o.getItemName()
                                                , o.getOrderPrice()
                                                , o.getCount()
                                        ), toList())))
                .entrySet()
                .stream()
                .map(e ->
                        new OrderQueryDto(
                                e.getKey().getOrderId()
                                , e.getKey().getName()
                                , e.getKey().getOrderDate()
                                , e.getKey().getOrderStatus()
                                , e.getKey().getAddress()
                                , e.getValue()))
                .collect(toList());
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
            orderItems =  order.getOrderItems().stream().map(OrderItemDto::new).collect(toList());

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
