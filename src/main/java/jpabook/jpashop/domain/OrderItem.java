package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; //OrderItem은 하나의 Item을 가짐, Item은 많은 OrderItem을 가짐

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // Order와 OrderItem 관계가 1:N관계이며 N이 OrderItem 이므로...
    private Order order;

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량

    /* 생성 메소드 */

    /**
     * 주문상품 생성
     */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);

        return orderItem;
    }

    /* 비즈니스 로직 */

    /**
     * 주문상품 취소
     */
    public void cancel() {
        // 재고 수량 원상복귀하기
        this.getItem().addStock(count);
    }

    /* 조회 로직 */

    /**
     * 주문상품 총 가격
     */
    public int getTotalPrice() {
        return this.getOrderPrice() * this.getCount();
    }
}
