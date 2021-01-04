package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberAPIController {
    private final MemberService memberService;

    /*
    가장 기본적인 형태의 조회 / 엔티티를 그대로 반환
    + 주문이 있으면 무한 참조 에러가 발생함
      (주문쪽에 @JsonIgnore 어노테이션을 붙여줘야 함)

    개발하다보면 위와 같이 @JsonIgnore 만으로는
    해결 불가능한 부분들이 생겨남

    이를테면 회원 조회를 할 때, 어딘가에서는 조회가 필요하고
    어딘가에서는 주소가 필요하고
    각자 필요하고 필요 없는 부분이 다름

    `@JsonIgnore` 라는 어노테이션을 쓴다는 자체가
    엔티티에 화면에 의존되는 어떤 관계 자체가 걸려버리는 것임

    결과적으로 엔티티를 직접 반환하면 안됨

    문제점 정리
    - 엔티티에 프레젠테이션 계층을 위한 로직이 추가됨
    - 엔티티의 모든 값이 노출됨
    - 응답 스펙을 맞추기 위해 로직이 추가됨 (`@JsonIgnore, 별도의 뷰 로직 등등..)
    - 실무에서는 같은 엔티티에 대해 API 가 용도에 따라 다양하게 만들어지는데,
      한 엔티티에 각각의 API 를 위한 프레젠테이션 응답 로직을 담기는 어려움
    - 엔티티가 변경되면 API 스펙이 변함
    - 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어려움

    결론
    - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.

    문제점 2로는 스펙 확장이 불가능하다.
    JSON의 형태 자체가

    ```
    [
      { ... }
    ]
    ```
    위와 같은 형태를 띄는 경우에는 스펙 확장이 불가능하므로

    ```
    {
      ...,
      data: {

      }
    }
    ```

    위와 같은 형태가 되어야 스펙 확장이 가능하다.

     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDTO {
        private String name;
    }

    /*
    조회는 DTO 를 통해 필요한 부분만 방어적으로 노출하는 것이 좋다.
    엔티티를 직접 파라미터로 받거나 반환하는 것을 '절대' 하면 안된다.
     */
    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();

        List<MemberDTO> collect = findMembers
                .stream()
                .map(m -> new MemberDTO((m.getName())))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
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
