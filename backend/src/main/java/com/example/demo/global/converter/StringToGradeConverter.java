package com.example.demo.global.converter;

import com.example.demo.domain.user.enums.Grade;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToGradeConverter implements Converter<String, Grade> {

    @Override
    public Grade convert(String source) {
        try {
            int intValue = Integer.parseInt(source);
            return Grade.fromValue(intValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 형식의 학년을 입력해주세요: " + source);
        }
    }
}
