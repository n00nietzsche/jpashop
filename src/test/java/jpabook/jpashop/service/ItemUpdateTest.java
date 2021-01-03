package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@ExtendWith(SpringExtension.class) // 테스트 시에 스프링과 같이 테스트하다는 것
@SpringBootTest // 스프링 컨테이너를 이용한 테스트를 하겠다는 것
@Transactional // 테스트 끝난 뒤 롤백을 위한 것
public class ItemUpdateTest {

    @Autowired EntityManager entityManager;

    @Test
    public void updateTest() throws Exception {
        // given
        Book book = entityManager.find(Book.class, 1L);

        // when
        // 변경 감지 (Dirty Checking)
        // 기존에 주문 취소할 때 이미 사용되었음.
        book.setName("바뀐 이름");

        // then
    }
}
