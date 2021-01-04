package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * 총 주문 2개
 * user A
 *   JPA1 BOOK
 *   JPA2 BOOK
 * user B
 *   SPRING1 BOOK
 *   SPRING2 BOOK
 */

@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitService initService;

    @PostConstruct
    /*
    `@PostConstruct` 는 DI 이후에 자동으로 실행될 메소드를 지정해주는 어노테이션이다.

    Spring Lifecycle 때문에,
    PostConstruct 에 `@Transaction` 먹이고 이런 것들이 잘 안되어서
    따로 클래스를 빼준 것이다.
     */
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager entityManager;

        public void dbInit1() {
            Member member = createMember("서울", "1", "1", "userA");
            entityManager.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            entityManager.persist(book1);

            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            entityManager.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            entityManager.persist(order);
        }

        public void dbInit2() {
            Member member = createMember("부산", "1", "1", "userB");
            entityManager.persist(member);

            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            entityManager.persist(book1);

            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
            entityManager.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            entityManager.persist(order);
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Book createBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }

        private Member createMember(String name, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }
    }
}
