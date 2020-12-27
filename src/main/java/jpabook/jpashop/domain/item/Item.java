package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
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
}
