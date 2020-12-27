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

    @OneToOne(fetch = FetchType.LAZY)
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
}
