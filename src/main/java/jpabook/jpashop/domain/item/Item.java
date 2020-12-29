package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// 3가지의 전략이 있는데, TABLE_PER_CLASS, JOINED, SINGLE_TABLE이 있다.
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {
    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    // 객체는 collection과 collection의 관계여서 중간 테이블이 없어도 다대다 관계가 가능한 반면,
    // 디비는 그게 불가능하기 때문에, 중간 연결 테이블이 있어야 한다.
    private List<Category> categories = new ArrayList<>();

    /* 비즈니스 로직 */

    /* 도메인 주도 설계를 할 때
    엔티티 안에서 해결될 수 있는 비즈니스 로직은
    엔티티 안에 비즈니스 로직을 넣어 해결하는 편이 좋다. */

    /* 객체지향적인 관점에서 봤을 때,
    값을 가지고 있는 객체가 해당 값을 컨트롤하는 비즈니스 로직을 갖는 것이 가장 좋다. */

    /* Setter를 이용하는 방법은 꼭 필요한 상황이 아니면 지양한다. */

    /**
     * 재고 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * 재고 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

}
