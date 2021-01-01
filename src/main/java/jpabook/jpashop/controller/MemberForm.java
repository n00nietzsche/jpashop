package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {
    /*
    @NotEmpty 어노테이션은 `hibernate-validator` 의존성을 통해서 사용할 수 있다.
    compile group: 'org.hibernate', name: 'hibernate-validator', version: '6.2.0.Final'

    손쉽게 Validation 을 체크할 수 있다.
     */

    @NotEmpty(message = "이름은 필수 입력 항목입니다.")
    private String name;
    private String city;
    private String street;
    private String zipcode;
}
