package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
패키지를 나눈 이유는
엔티티를 조회하는 것과
API 에 의존 관계가 있는 것의 기준으로 나눈 것임
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager entityManager;

    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> orders = findOrders();

        List<Long> orderIds = orders.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());

        /*
            findOrderItems를 가져와서 기존 orderId 로
            조건을 걸어 데이터를 가져오던 방식을 SQL IN 절로 변경함
             -> IN 쿼리에 List 파라미터를 주면, 하나하나씩 IN 을 찍음
         */
        List<OrderItemQueryDto> orderItems = entityManager.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto" +
                        "(" +
                        "oi.order.id" +
                        ", oi.item.name" +
                        ", oi.orderPrice" +
                        ", oi.count" +
                        ")" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds"
                , OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        /*
        orderItems 를 Id를 가진 맵으로 변경함
         */
        Map<Long, List<OrderItemQueryDto>> orderItemsMap = orderItems
                .stream()
                .collect(
                        Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()
                        )
                );

        /*
        메모리에 올려진 Map에 들어있는 OrderItems는 Order.id를 키로 하고 있는데,
        그 키와 매칭시켜서 OrderItem을 세팅해줌
          -> 쿼리가 총 2번 나감
         */
        orders.forEach(o -> o.setOrderItems(orderItemsMap.get(o.getOrderId())));

        return orders;
    }

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        /*
        OrderItem에서 ID를 가져오려면, `oi.order.id`를 하면 되는데,
        실제로 order객체를 또 select해서 가져오지 않고,
        foreign key로 쓰이던 값을 바로 가져온다.

        여기서 일반 join을 쓴 이유는
        join의 목적이 엔티티 객체와 객체 그래프를 함께 조회하는 것이 아닌
        데이터베이스가 제공하는 그냥 join을 쓰고싶기 때문이다.

        `join fetch`는 엔티티를 직접 조회할 때 쓰게 되고
        `join`은 `DTO`를 이용한 조회를 할 때 쓰이게 된다.
        현재 우리가 만든 `DTO`에서는 엔티티로의 관계가 없으니, `join fetch` 쓰면 에러난다.

        조회는 총 N+1 번이 된다.
         */
        return entityManager.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto" +
                        "(" +
                        "oi.order.id" +
                        ", oi.item.name" +
                        ", oi.orderPrice" +
                        ", oi.count" +
                        ")" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId"
                , OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /*
    OrderItems는 일단 구조상 넣을 수 없음
      -> findOrderQueryDto로 위임
     */
    public List<OrderQueryDto> findOrders() {
        return entityManager.createQuery(
                "select new " +
                        "jpabook" +
                        ".jpashop" +
                        ".repository" +
                        ".order" +
                        ".query" +
                        ".OrderQueryDto" +
                        "(" +
                        "o.id" +
                        ", m.name" +
                        ", o.orderDate" +
                        ", o.status" +
                        ", d.address" +
                        ") from Order o" +
                        " join o.member m" +
                        " join o.delivery d"
                , OrderQueryDto.class)
        .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_flat() {

    }
}
