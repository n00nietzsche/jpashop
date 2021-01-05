package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 *
 * 무언가 1개로 맵핑되는 관계들
 */

@RequiredArgsConstructor
@RestController
public class OrderSimpleAPIController {
    private final OrderRepository orderRepository;

    /*
    무한루프에 빠지게 됨
    계속 Order 와 Member 가 서로를 참조함$
      -> 양방향 연관관계가 있을 때 한 쪽으로는 `@JsonIgnore`를 해주어야 함
        -> 그래도 에러가 남.
          -> 지연로딩이기 때문에 Member 에 대한 데이터는 손을 안댐
             그래서 JPA 에서 임시로 Proxy(new Member()) 와 같이
             프록시를 만들어서 감싸줌. 이것이 `bytebuddy`가 됨
               -> 에러가 나지 않게 해당 객체를 뿌리지 않도록 설정해주어야 하는데
                  Hibernate 5 Module 로 해결이 가능함
                  https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-hibernate5
                  implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
                  버전은 스프링부트가 최  적화된 버전을 갖고 있기 때문에 생략해보는 것이 좋을 수 있다.
                    -> 지연로딩되는 것들을 다 null로 처리함
     */

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> orderList = orderRepository.findAllByString(new OrderSearch());
        for (Order order : orderList) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return orderList;
    }

    /*
    정리

    - 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭 한곳을 `@JsonIgnore`처리해야 한다.
    안그러면 서로가 서로를 호출해서 무한 루프에 빠진다

    - 앞에서 계속 강조했듯이, 정말 간단한 애플리케이션이 아니면 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다.
    따라서 `Hibernate5Module`을 사용하기 보다는 DTO 로 변환해서 반환하는 것이 더 좋은 방법이다.

    - 지연 로딩(Lazy)을 피하기 위해 즉시 로딩(Eager)으로 설정하면 안된다!
    (Eager 로 하면 당장은 되지만 나중에 분명 성능 이슈가 생김 또한 성능 최적화를 할 수 있는 여지가 사라짐)
    즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다.
    즉시 로딩으로 설정하면 성능 튜닝이 매우 어려워진다.
    항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 페치조인을 사용해야 한다! (V3에서 설명)
     */

    @GetMapping("/api/v2/simple-orders")
    /*
    기본적으로 List 로 바로 반환하는 것은
    배열로 시작하는 유연성 낮은 JSON 구조가 들어가게 되므로
    선호되지 않는다.
     */
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orderList = orderRepository.findAllByString(new OrderSearch());
        return orderList.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
    }

    /*
    이렇게 DTO 를 따로 생성해주면,
    엔티티의 내용이 바뀌더라도 컴파일 전에 에러를 쉽게 캐치할 수 있다.

    그런데 V2의 문제는 Query 를 너무 많이 날린다는 문제가 있다.
    처음 Select 로 총 2개의 주문을 가져오게 되는데,
    각 2개의 주문에 대한 Delivery 와 Member 의 Name 을 가져오기 때문에
    각각의 주문마다 2개의 쿼리가 더 나가는 것이기 때문에
    최악의 경우 총 5개의 쿼리가 나간다.

    단, JPA 에서는 영속성 컨텍스트가
    이미 데이터를 갖고 있는지에 대해서 찔러보기 때문에
    만일, 모든 주문에서 같은 Member ID를 갖고 있으면, (모든 주문이 같은 멤버에 의해 이뤄졌으면)
    1번의 조회에서 모든 Member 의 이름이 구해질 수도 있다.


    그렇다고 EAGER 방식으로 해도 쿼리가 최적화되진 않는다.
    더군다나 EAGER 를 쓰면 예측 불가능한 쿼리를 만들어내기도 한다.

    이후에 이것을 `join fetch` 으로 바꿔주는 것이 좋다.
     */
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
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
    }

    /*
    V2 버전과 쿼리가 다르게 날아감

    inner join 을 두번 이용한 쿼리가 날아가서 성능적으로 이득이 많다.
    (일반적으로 성능 문제는 네트워크에서 병목이 일어나는 경우가 많은데 그런 경우를 해결할 수 있다)
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orderList = orderRepository.findAllWithMemberDelivery();
        return orderList.stream().map(SimpleOrderDto::new).collect(Collectors.toList());
    }
}
