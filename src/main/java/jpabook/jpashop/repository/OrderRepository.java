package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager entityManager;

    public void save(Order order) {
        entityManager.persist(order);
    }

    public Order findOne(Long id) {
        return entityManager.find(Order.class, id);
    }

    // 검색기능은 동적쿼리가 필요하기 때문에 나중에 설명

    /*
    질문: 아래와 같이 짰을 때,
    만일 값이 있으면 조건이 있는 쿼리로 수행하고,
    없으면 조건을 해제하여 수행하는 쿼리를 어떻게 수행해야 하는가?
    -> 동적쿼리 문제

    // 조인은 SQL 조인문과 비슷하게 생각하면 된다.
    return entityManager.createQuery("select o from Order o join o.member m" +
            " where o.status = :status " +
            " and m.name like :name"
            , Order.class)
            .setParameter("status", orderSearch.getOrderStatus())
            .setParameter("name", orderSearch.getMemberName())
            //.setFirstResult(100) // 페이징을 하고 싶다면, 앞의 `setFirstResult`와 뒤의 `setMaxResult`를 이용하면 된다.
            .setMaxResults(1000)
            .getResultList();
    */

    /*
    첫번째 해결방법, 단순히 자바의 if문을 이용하여 문자열을 생성한다. (비추천)

    String jpql = "select o from Order o join o.member m";
    boolean isFirstCondition = false;

    // 주문 상태 검색...
    if(orderSearch.getOrderStatus() != null) {
        if (isFirstCondition) {
            jpql += " where";
            isFirstCondition = false;
        } else {
            jpql += " and";
        }

        jpql += " o.status = :status";
    }

    return entityManager.createQuery(jpql, Order.class)
            .setParameter("status", orderSearch.getOrderStatus())
            .setParameter("name", orderSearch.getMemberName())
            //.setFirstResult(100) // 페이징을 하고 싶다면, 앞의 `setFirstResult`와 뒤의 `setMaxResult`를 이용하면 된다.
            .setMaxResults(1000)
            .getResultList();

    사실 이 방법을 이용하면, `setParameter()` 메소드도 동적으로 생성해주어야 한다.

    이렇게 코드를 작성하면, 문자열을 생으로 이용하기 때문에 버그가 너무나 쉽게 발생할 수 있다.
     */

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
//주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
//회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class) .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }


    /**
    두번째 해결방법 JPA Criteria 를 이용한다. (비추천)

     이 방법은 이전 방법보다 버그는 적을 수 있으나
     유지보수하기가 매우 어렵다는 단점이 있다.
     코드가 너무 길어지고, 한눈에 어떤 쿼리인지 알아보기가 너무 힘들다.
    */
    /*
    public List<Order> findByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaBuilderQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> orderRoot = criteriaBuilderQuery.from(Order.class);
        Join<Object, Object> memberJoin = orderRoot.join("member", JoinType.INNER);

        // 여기서 동적 쿼리에 대한 커넥션 조합을 깔끔하게 만들 수 있다.
        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            Predicate status = criteriaBuilder.equal(orderRoot.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = criteriaBuilder.like(memberJoin.get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        criteriaBuilderQuery.where(criteriaBuilder.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = entityManager.createQuery(criteriaBuilderQuery).setMaxResults(1000);
        return query.getResultList();
    }
     */

    /**
     * QueryDSL로 처리
     */

    /*
    동적쿼리, 정적쿼리를 위해서 모두 유용하다.
    실무에서는 스프링부트, JPA, SPRING-JPA-DATA, QUERY DSL을 모두 사용하는 것이 좋다.
     */




    /*
    > 보통은 이렇게 길게 이름을 짓진 않는다.

    LAZY 를 무시하고 값을 다 채워서 가져오는 방식
    이것을 `fetch join` 이라고 부른다.

    `fetch join`은 100% 이해해야 실무에서 쓸 수 있다.
     */
    public List<Order> findAllWithMemberDelivery() {
        return entityManager.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d",
                Order.class
        ).getResultList();
    }

    /*
    V3.1 추가

    참고로 아래의 메소드는 페이징을 해도 아무런 문제가 없다.
    `1:N` 관계인 컬렉션을 페치 조인 했을 때 문제가 발생하는 것이지,
    `x:1` 로 즉, 한 row 로 표현되는 관계는 페치 조인을 해도 페이징이 정상적으로 가능하다.
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return entityManager.createQuery(
        "select o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d"
                , Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /*
    나중에 실무에서는 이렇게 약간이라도 복잡해지는 경우에는
    스트링으로 작성하기보다 QueryDSL 을 이용하는 편이 좋다.

    아래와 같이 작성하면,
    데이터베이스 입장에서 Order 와 OrderItem 을 그대로 조인해버려서
    Order 의 데이터 개수가 OrderItem 만큼 늘어난다.

    Order 의 개수가 OrderItem 만큼 늘어나도,
    Hibernate 입장에서는 Order 가 늘어난 것인지 알 수 없다.
    Hibernate 는 그냥 데이터를 받은 그대로 받아들이고,
    결과적으로 API 출력 JSON 에는 중복된 Order 가 그대로 들어간다.
    > 중복된 Order 의 경우 Java 의 Reference 참조 값까지 동일하다.

      -> 위와 같은 현상을 해결하려면 `select` 옆에 `distinct` 키워드를 붙여주면 된다.
         `distinct` 키워드는 실제로 DB의 select 문에 distinct 를 추가해주고,
         Java 객체에서도 레퍼런스가 같은 객체를 없애준다.
         하지만, `distinct`를 써도 Java 상에서는 중복이 제거되지만, DB 에서는 안된다.
         왜냐하면 DB의 `distinct`는 모든 컬럼의 값이 일치해야만 중복 제거를 하기 때문이다.
           -> 실제로 DB 에서 쿼리를 직접 날려보면 실제로는 4개의 ROW 가 출력되는 것을 볼 수 있다.

     이렇게 쿼리를 `join fetch`로 변경해주면, 이전(쿼리 11번)과는 다르게 쿼리 1번에 처리 가능하다.
     마이바티스와 같은 환경에서 동일한 기능을 코딩한다고 하면, 일일이 최적화된 쿼리를 작성해주어야 하는데,
     그와 비교해서 이렇게 JPA 의 join fetch 를 이용하여 처리하는 것은 매우 효율적이다.

     ## 정리
     - 페치 조인으로 SQL 이 한 번만 실행됨
     - `distinct`를 사용한 이유는 1대다 조인이 있으므로, 데이터베이스 `ROW`가 증가한다.
       그 결과 order 엔티티의 조회 수도 증가한다. `JPA`의 `distinct`는 `SQL`에 `distinct`를 추가하고,
       더해서 같은 엔티티가 조회되면, 애플리케이션에서 중복을 걸러준다.
       이 예에서 order 가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아준다.
     - 단점으로 **페이징이 불가능하다.** (매우 중요하다.)

     ## 페이징 하면?

     ```
     .setFirstResult(1)
     .setMaxResults(100)
     ```

     위와 같은 메소드들을 추가해줌으로써 페이징을 할 수 있다.

     그런데 막상 페이징을 하면?
     > `2021-01-06 11:30:09.696  WARN 456436 --- [nio-8080-exec-2] o.h.h.internal.ast.QueryTranslatorImpl   : HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!`

     위와 같은 로그메세지와 함께 페이징이 메모리에서 처리된다.
     데이터가 몇 건 안될 때는 충분히 메모리에서 처리할 여력이 되지만,
     많은 데이터가 존재할 때는 join 때문에 불어난 전체 Row 수를 전부 메모리에 올려야 하므로
     어마무시하게 많은 부하가 걸리게 된다.

     ## 컬렉션 페치 조인은 1개만 사용 가능

     컬렉션 둘 이상에 페치 조인을 사용하면,
     join 에 의해 늘어나는 과정이 두 번 거쳐지게 된다.
     이렇게 되면 데이터가 부정합하게 조회될 수 있으므로 주의해야 한다.
     */
    public List<Order> findAllWithItem() {
        return entityManager.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i"
                , Order.class)
                .setFirstResult(0)
                .setMaxResults(100)
                .getResultList();
    }

}
