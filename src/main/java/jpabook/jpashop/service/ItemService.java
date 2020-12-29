package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    /*
    이번 개발은 위임만하면 끝이라, 정말 이런 서비스를 만들어야될지에 대한 고민도 해볼만하다.
    위임만할 때는 따로 서비스 클래스를 개발하지 않고, 리포지토리를 쓰고 비즈니스로직이 포함된 것만 서비스를 쓰는 것도 괜찮을 것 같다.
     */

    @Transactional // 메소드에 가까운 것이 오버라이드된다.
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
