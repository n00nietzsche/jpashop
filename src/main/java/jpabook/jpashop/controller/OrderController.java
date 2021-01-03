package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model) {
        List<Member> memberList = memberService.findMembers();
        List<Item> itemList = itemService.findItems();

        model.addAttribute("members", memberList);
        model.addAttribute("items", itemList);

        return "order/orderForm";
    }

    @PostMapping("/order")
    /*
    Form submit 에서 name 으로 지정된 값은 `@RequestParam`에서 받을 수 있음.
     */
    public String order(@RequestParam("memberId") Long memberId
                        , @RequestParam("itemId") Long itemId
                        , @RequestParam("count") int count)
    {
        /*
        가급적 내용 변경이 들어가는 비즈니스 로직에 대해서는
        영속성 컨텍스트가 존재하지 않는 컨트롤러에서 보다는
        영속성 컨텍스트가 존재하는 Service와 같은 곳에서 진행하자.
         */
        orderService.order(memberId, itemId, count);

        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(
            @ModelAttribute("orderSearch") OrderSearch orderSearch,
            Model model) {
        /*
        단순한 조회 기능일 경우에는 `orderRepository`에서 바로 조회하는 것도 나쁜 패턴은 아니다.
         */
        List<Order> orderList = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orderList);
        /*
        @ModelAttribute("orderSearch") OrderSearch orderSearch 의 의미는
        model.addAttribute("orderSearch", orderSearch)와 같이, `orderSearch`의 정보를 뷰로 넘기는 의미를 갖고 있다.
        model.addAttribute -> modelAttribute로 이해하자.
         */

        return "/order/orderList";

        /*
        나는 처음에 Order 엔티티에서 `cascade = CascadeType.ALL`을 빼먹었는데,
        그래서 에러가 났었다.

        CascadeType.ALL 을 빼먹으면

        `org.hibernate.TransientPropertyValueException:
        object references an unsaved transient instance
        - save the transient instance before flushing :
        jpabook.jpashop.domain.Order.delivery ->
        jpabook.jpashop.domain.Delivery;`

        위와 같은 에러가 난다. transient instance 라면서 안된다.
        transient 란 이전에 배웠었는데, 엔티티 안에 컬럼으로서 존재하는 것이 아니라,
        값으로서 존재하는 것읆 말한다.

        아마 `CascadeType.ALL`을 적어주지 않으면, 값으로서 받아들이는 것 같다.

        @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @JoinColumn(name = "delivery_id")
        private Delivery delivery;
         */
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }



}
