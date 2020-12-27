package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    // 타입에 Long을 쓰는 이유는, 엔티티를 생성하고, JPA를 통해 DB에 저장하는 시점이 되어야 값이 설정되기 때문이다.
    // 그래서 null을 유지할 수 있는 상태가 필요하다.

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")
    // 관계의 주인이 내가 아님을 이야기함
    // 단순히 종속된다는 뜻
    // member를 이용해서 값을 비춰주기만 하고 관련된 값에 대한 아무런 수정 권한이 없음
    // JPA의 규약임, 어떤 값을 업데이트할지 결정하기 어려운 경우가 있기 때문.
    private List<Order> orders = new ArrayList<>();
}
