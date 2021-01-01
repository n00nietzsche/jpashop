package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

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

}
