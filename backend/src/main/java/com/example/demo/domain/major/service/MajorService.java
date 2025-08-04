package com.example.demo.domain.major.service;

import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.major.response.MajorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;

    public List<MajorResponse> getAllMajors() {
        return majorRepository.findAll().stream()
                .map(MajorResponse::from)
                .collect(Collectors.toList());
    }
}
