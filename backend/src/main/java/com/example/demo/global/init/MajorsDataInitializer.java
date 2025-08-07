package com.example.demo.global.init;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MajorsDataInitializer {

    private final MajorRepository majorRepository;

    @PostConstruct
    public void initMajors() {
        if (majorRepository.count() == 0) {
            Major major1 = Major.builder()
                    //.id(UUID.randomUUID())
                    .name("컴퓨터공학과")
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build();
            Major major2 = Major.builder()
                    //.id(UUID.randomUUID())
                    .name("전자공학과")
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build();

            majorRepository.saveAll(List.of(major1, major2));
        }
    }

}
