package jpabook.jpashop.service;

import com.sun.xml.txw2.IllegalSignatureException;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor // final이 있는 필드만 가지고 생성자를 만들어주는 것
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 중복회원 검증
     */
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());

        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 한 건 조회
     */
    public Member findOne(Long memberId) {
        return memberRepository.findById(memberId).get();
    }

    /**
     * 회원 이름 업데이트
     */
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findById(id).get();
        // Transaction 이 끝날 때, Dirty Check 에 의해서 변경
        member.setName(name);

        /*
        여기에 있는 Member 객체를 반환하게 되면,
        Command 와 Query 가 구분되지 않음

        해당 원칙을 기준으로 프로그래밍하면 권장하지 않음
        */
    }
}
