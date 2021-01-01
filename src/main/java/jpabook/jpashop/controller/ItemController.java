package jpabook.jpashop.controller;

import jpabook.jpashop.controller.form.BookForm;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    @GetMapping(value = "/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }
    @PostMapping(value = "/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);
        return "redirect:/items";
    }

    /**
     * 상품 목록
     */
    @GetMapping(value = "/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    /*
    여기서는 `@PathVariable` 어노테이션을 사용해서 동적 라우팅을 한다.
    `@RequestParam`을 이용해서도 값을 받아올 수 있는데 둘은 방식이 좀 다르다.

    - `@RequestParam`은 쿼리 스트링에서 값을 추출한다. (a.com?foo=1)
    - `@PathVariable`은 URI 경로에서 값을 추출한다. (a.com/foo/1)

    - `@RequestParam`은 URL Decoded 값을 가져옴
    - `@PathVariable`은 Encoded 값을 그냥 가져옴

    - 둘 다 (`required = false` 옵션을 통해 필수 값이 아닌 옵션 값으로 설정이 가능함)
     */
    @GetMapping(value = "items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        /* 사실 이렇게 캐스팅하는 것이 선호되는 패턴은 아니다. */
        Book book = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setName(book.getName());
        form.setPrice(book.getPrice());
        form.setStockQuantity(book.getStockQuantity());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());

        model.addAttribute("form", form);
        /* 일단은 데이터를 뿌려주자. */
        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    /*
    ModelAttribute 는 입력을 오브젝트 형태로 받기 위해서 쓰는 어노테이션이다.
     */
    public String updateItem(@ModelAttribute("form") BookForm bookForm, @PathVariable("itemId") Long itemId) {

        /*
        실무에서는 Id에 대한 여러가지 취약점이 존재해서 반드시 주의해야 한다.
        악의적인 사용자가 Id를 바꾸어 보낼 수 있기 때문에,
        Service와 같은 뒷단에서 권한체크를 한번 더 해주어야 한다.
        */
        Book book = new Book();

        book.setId(bookForm.getId());
        book.setName(bookForm.getName());
        book.setPrice(bookForm.getPrice());
        book.setStockQuantity(bookForm.getStockQuantity());
        book.setAuthor(bookForm.getAuthor());
        book.setIsbn(bookForm.getIsbn());

        itemService.saveItem(book);

        return "redirect:/items";
    }

}