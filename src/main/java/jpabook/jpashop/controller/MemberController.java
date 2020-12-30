package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        // 이 데이터를 가지고 가는 이유는 Validation 을 위해서임
        // 화면에서 MemberForm 객체에 대한 접근이 가능해진다.
        model.addAttribute("memberForm", new MemberForm());

        return "members/createMemberForm";
    }

    // `@Valid` 어노테이션을 사용하면 이전에 적용했던
    // `@NotEmpty` 와 같이, 클래스에 적용할 수 있는
    // 각종 Validation 어노테이션들을 사용할 수 있다.
    // (Validation에 이용 가능한 정의 파일들 보여주기)
    //
    // BindingResult가 있으면, 코드의 진행방향이 그냥 Error를 던지는 것이 아니라
    // Error가 나더라도 내가 설계한 방향으로 보낼 수 있다.
    //
    // Member 엔티티와 MemberForm 을 따로 나눠야 하는 이유는
    // 컨트롤러에서 요구하는 요구사항과 엔티티에서 요구하는 요구사항이 다르기 때문이다.
    // 억지로 엔티티에 컨트롤러의 요구사항을 넣다보면 엔티티가 너무 복잡해질 수 있다.
    @PostMapping("/members/new")
    public String create(@Valid MemberForm memberForm, BindingResult bindingResult) {

        // BindingResult 에는 hasErrors() 말고도 많은 유용한 메소드들이 있다.
        // 이렇게 BindingResult 를 이용하여 에러를 검출한 뒤에 Thymeleaf 화면에도 적용시켜줄 수 있다.
        if(bindingResult.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipcode());

        Member member = new Member();
        member.setName(memberForm.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }
}
