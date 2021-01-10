package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl {

    private final EntityManager entityManager;

    /**
     * 회원 저장
     */
    public void save(Member member) {
        entityManager.persist(member);
    }

    /**
     * 회원 찾기
     */
    public Member findOne(Long id) {
        return entityManager.find(Member.class, id);
    }

    /**
     * 회원 리스트 불러오기
     */
    public List<Member> findAll() {
        return entityManager.createQuery("select m from Member m", Member.class).getResultList();
    }

    /**
     * 해당 이름을 가진 회원 리스트 불러오기
     */
    public List<Member> findByName(String name) {
        return entityManager.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
