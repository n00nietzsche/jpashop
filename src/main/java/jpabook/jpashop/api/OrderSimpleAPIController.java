package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    계속 Order 와 Member 가 서로를 참조함
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

}
