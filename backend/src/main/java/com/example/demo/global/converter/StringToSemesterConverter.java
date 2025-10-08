package com.example.demo.global.converter;

import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import org.springframework.core.convert.converter.Converter;

public class StringToSemesterConverter implements Converter<String, Semester> {

    @Override
    public Semester convert(String source) {
        try {
            int intValue = Integer.parseInt(source);
            return Semester.fromValue(intValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 형식의 학기를 입력해주세요: " + source);
        }
    }
}
