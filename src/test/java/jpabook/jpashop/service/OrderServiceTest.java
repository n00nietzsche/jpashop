package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class) // 테스트 시에 스프링과 같이 테스트하다는 것
@SpringBootTest // 스프링 컨테이너를 이용한 테스트를 하겠다는 것
@Transactional // 테스트 끝난 뒤 롤백을 위한 것
class OrderServiceTest {

    @Autowired EntityManager entityManager;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    /*
    사실 좋은 테스트는 JPA, 스프링 부트, DB 등에 의존하지 않고,
    독립적으로 동작하는 테스트가 좋은 테스트라고 볼 수 있다.
     */
    @Test
    public void orderItem() throws Exception {
        //given
        Member member = createMember();

        int bookPrice = 10000;
        int bookQuantity = 10;
        Book book = createBook("시골 JPA", bookPrice, bookQuantity);

        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(bookPrice * orderCount, getOrder.getTotalPrice(), "주문 가격은 가격 * 수량이다.");
        assertEquals(bookQuantity - orderCount, book.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다.");
    }

    /*
    참고: CTRL + SHIFT + 방향키 위 아래로 소스코드 전체 움직이기 가능
     */
    // 사실 이러한 통합테스트보다 mock을 통해 메소드에 대한 단위테스트로 테스트하는 것이 더 좋은 테스트이다.
    @Test
    public void stockOverflow() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderQuantity = 11;

        //when
        assertThrows(NotEnoughStockException.class, () -> {
            orderService.order(member.getId(), book.getId(), orderQuantity);
        }, "재고 부족 예외가 발생해야 한다.");
    }

    @Test
    public void cancelOrder() throws Exception {
        //given
        Member member = createMember();
        Book item = createBook("JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
        assertEquals(8, item.getStockQuantity(), "주문한 뒤에 당장은 8개여야 한다.");

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 CANCEL이다.");
        assertEquals(10, item.getStockQuantity(), "주문이 취소된 상품의 재고는 다시 복구되어야 한다.");
    }


    /*
    참고: Extract Variable -> Ctrl+Alt+V, Extract Method -> Ctrl+Alt+M, Extract Parameter -> Ctrl+Alt+P
     */
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        entityManager.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        entityManager.persist(member);
        return member;
    }
}