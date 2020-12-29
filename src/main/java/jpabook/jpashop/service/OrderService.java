package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional // 데이터에 변경이 일어난다.
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문상품 생성
        // 이렇게 생성 메소드가 있는 경우에는 기본생성은 막는 것이 좋다
        // new OrderItem()과 같은 식으로 생성하다보면 나중에 소스코드가 퍼졌을 때 관리하기 어려울 수 있다.
        // 생성 메소드륾 막을 때는
        // 롬복의 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 어노테이션이 유용하다.
        // 항상 필요한 부분만 사용하고 나머지는 제약하는 스타일이 유지보수에 용이하다.
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        // 저장을 한번만 해도 되는 이유는 Cascade 옵션 때문이다.
        // OrderItem과 Delivery 엔티티에 대해서도 save가 적용된다.
        // 참조하는 주인이 private owner일 때만 사용하는 것이 좋다.
        // OrderItem이나 Delivery는 이번 프로젝트에서 Order에서만 쓰인다.
        // 만일 다른 테이블에서 OrderItem이나 Delivery를 참조한다면,
        // 다른 테이블에서 참조하는 엔티티가 변할 수도 있기에 주의해야 한다.
        // 이 방법에 대해 잘 모르면 아예 안쓰다가 조심조심 리팩토링 해보는 것이 좋다.
        orderRepository.save(order);
        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 취소 (기존에 만들었던 엔티티 내부메소드로 처리)
        // JPA의 진짜 장점은 이 과정에서 생기는 여러가지 update 쿼리를 작성하지 않아도 된다는 것이다.
        // 엔티티 내부메소드로 엔티티 내부의 데이터를 Set 하게 되면 업데이트 쿼리가 자동으로 날아간다.
        // 아래 취소의 경우에는 Order와 OrderItem에 업데이트 쿼리가 날아갈 것이다.
        order.cancel();
    }

    /**
     * TODO: 주문 검색
    */
    /*
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }
    */

    /*
    참고: 주문 서비스의 주문과 주문 취소 메소드를 보면 비즈니스 로직이 대부분 엔티티에 있다.

    서비스계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다.
    이처럼 엔티티가 비즈니스 로직을 갖고 객체지향의 특성을 적극 활용하는 것을 도메인 모델 패턴이라고 한다.
    [도메인 모델 패턴 참고 링크](http://martinfowler.com/eaaCatalog/domainModel.html)
    JPA를 사용할 때는 도메인 모델 패턴을 많이 사용하게 된다.

    반대로 엔티티에는 비즈니스로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을
    트랜잭션 스크립트 패턴이라 한다.
    [트랜잭션 스크립트 패턴 참고 링크](http://martinfowler.com/eaaCatalog/transactionScript.html)
    트랜잭션 스크립트 패턴은 일반적으로 SQL을 많이 사용할 때 쓰게 되는 패턴이다.

    두 패턴의 장단점은 스스로 고민해보아야 한다.
    한 프로젝트 내에서도 두 패턴이 양립하는 경우가 많다.
     */
}
