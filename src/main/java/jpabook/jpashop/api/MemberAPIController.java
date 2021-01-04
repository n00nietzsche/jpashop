package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberAPIController {
    private final MemberService memberService;

    /*
    가장 기본적인 형태의 조회 / 엔티티를 그대로 반환
    + 주문이 있으면 무한 참조 에러가 발생함
      주문쪽에 @JsonIgnore 어노테이션을 붙여줘야 함
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }


    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest createMemberRequest) {
        Member member =  new Member();
        member.setName(createMemberRequest.getName());

        Long memberId = memberService.join(member);
        return new CreateMemberResponse(memberId);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest updateMemberRequest) {

        memberService.update(id, updateMemberRequest.getName());
        /*
        커맨드 쿼리 분리 원칙을 지키기 위해서
        업데이트에서 멤버 엔티티를 그대로 반환하기보다
        새로 엔티티를 찾아서 반환함
        */
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());

    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }
}
