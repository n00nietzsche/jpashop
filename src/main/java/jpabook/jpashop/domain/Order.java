package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id") // foreign_key의 이름
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // 1:1 관계에서는 foreign key를 두는 곳을 선택할 수 있음.
    // 그러면 주로 많이 이용하는 테이블에 foreign key를 둠
    // 여기서는 Order가 많이 이용될 것 같으니, (배송 정보로 주문을 찾는 일은 거의 없음)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // 기존 Date 타입은 따로 어노테이션을 더해줬어야 했는데,
    // Java 8 이후부터 LocalDateTime을 사용하면 자동으로 하이버네이트가 지원을 해준다.
    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 [ORDER, CANCEL]

    // 연관관계 편의 메소드
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /*
    생성 메소드
    */

    /* Order 엔티티를 생성하는데는 간단히 문자열이나 숫자만 들어가는 것이 아니라, 다른 엔티티들을 매개변수로 넣어주어야 한다.
    * 이렇게 복잡한 엔티티가 있는 경우에는 보통 엔티티 내부에서 엔티티 생성을 쉽게할 수 있는 메소드를 만들어준다.
    *
    * 이런 스타일로 작성하는 것이 중요한 이유는 앞으로 생성에 문제가 생겼을 때, 이 메소드만 보면 해결이 된다. */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);

        // 실무에서는 OrderItem이 DTO를 타고 오거나, 이 생성 메소드 내부에서 생성될 수도 있다.
        // 경우에 따라서는 그게 깔끔할 수도 있으니 참고하자.
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    /*
    비즈니스 로직
    */

    /**
     * 주문 취소
     */
    public void cancel() {
        /*
        이 부분은 따로 merge 등이 없었어도 잘 수정되어 반영되었었다.
         */
        if(delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);

        /* 내부 멤버 변수라서 따로 `this` 키워드를 적을 필요는 없지만, 그래도 적어준다. */
        for (OrderItem orderItem: this.orderItems) {
            orderItem.cancel();
        }
    }

    /*
    조회 로직
    */

    /**
     * 전체 주문가격 조회
     */
    public int getTotalPrice() {
        return this.orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }

}
