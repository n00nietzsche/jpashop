package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager entityManager;

    /*
    자동완성 때문에 잠시 컨트롤러에 의존관계가 생겼었는데,
    리포지토리에서는 절대 컨트롤러에 의존관계가 있으면 안된다.
    의존관계는 한 방향으로 흘러가야 함

    JPA 는 기본적으로 엔티티나 Value Object 정도만 반환할 수 있다.
    그래서 특정한 클래스를 반환하려면 생성자와 jpql 을 좀 손봐야 한다.

    생성자를 손보지 않고, 클래스 자체를 넣어버리면
    JPA 에서는 기본적으로 해당 클래스를 식별자로 인식해서 넣어버린다.
    (테스트 결과 N+1 문제가 그대로 일어난다.)

    > Delivery 의 Address 와 같은 Value Type 은 그냥 쿼리에 넣어도 잘 된다.

    V3와 V4의 차이는 필요한 컬럼만 가져오는지
    조인의 모든 컬럼을 가져오는지에 대해 차이가 있다.

    V3와 V4의 우열은 가릴 수 없다.

    V3는 재사용성이 높지만 최적화가 V4보다는 덜하고
    V4는 재사용성이 낮지만 최적화가 V3보다 좋다 (+ 코드가 더 지저분하다)

    - 일반적인 SQL을 사용할 때처럼 원하는 값을 선택해서 조회
    - `new` 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 반환
    - Repository의 본질적인 목적을 벗어남
      - 엔티티의 객체 그래프들을 조회할 때 사용돼야 함 (DTO를 조회하는 것이 아님)
      - 논리적 계층이 다 깨져있는 것으로도 볼 수 있음
      - 리포지토리가 화면을 의존하게 됨 -> 화면이 바뀌면 리포지토리를 바꿔야 함
    - SELECT에서 원하는 데이터를 직접 선택하므로 네트웍 성능이 조금 늘지만, 사실 성능차이가 거의 안남
      - 단, 컬럼이 매우 많은 테이블에서는 유의미하게 작용할 수 있음

    > 이와 같이 극한의 최적화를 한 Repository는 보통 패키지 자체를 따로 나누어 두어 구분한다.

    ## 정리

    엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다.
    둘 중 상황에 따라서 더 나은 방법을 선택하면 된다.
    엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다.
    따라서 권장하는 방법은 다음과 같다.

    **쿼리 방식 선택 권장 순서**
- 엔티티를 DTO로 변환하는 방법을 선택한다.
  - 유지보수에 매우 용이
- 필요하면 페치 조인으로 성능을 최적화한다. -> 대부분의 성능이슈가 해결된다.
- 그래도 성능 문제가 있다면, DTO로 직접 조회하는 방법을 선택한다.
- 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return entityManager.createQuery(
                "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto" +
                        "(" +
                        "o.id" +
                        ", m.name" +
                        ", o.orderDate" +
                        ", o.status" +
                        ", d.address" +
                        ")" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d"
                , OrderSimpleQueryDto.class
        ).getResultList();
    }
}
