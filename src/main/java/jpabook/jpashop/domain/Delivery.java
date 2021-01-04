package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Delivery {
    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    // enum일 때는, @Enumerated 어노테이션 반드시 추가해주고,
    // ORDINAL, STRING 중에 선택해야 하는데,
    // 반드시 STRING으로 해야 중간에 무언가 추가되어도 문제가 없다.
    private DeliveryStatus status;
}
