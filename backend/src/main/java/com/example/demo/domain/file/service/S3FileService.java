package com.example.demo.domain.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${custom.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) {
        try {
            // 파일명 생성 (UUID + 원본 파일명)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "posts/" + UUID.randomUUID() + extension;

            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            // 업로드된 파일의 URL 반환
            return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucket, fileName);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    // 사용자 프로필 이미지 업로드 (users/ prefix)
    public String uploadUserProfileImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "users/" + UUID.randomUUID() + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucket, fileName);
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * S3 파일의 Presigned URL을 생성합니다.
     * @param s3Url S3 객체의 전체 URL (예: https://bucket.s3.region.amazonaws.com/key)
     * @return Presigned URL
     */
    public String generatePresignedUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) {
            return null;
        }

        try {
            // S3 URL에서 bucket과 key 추출
            // 예: https://bucket.s3.ap-northeast-2.amazonaws.com/posts/uuid.jpg
            String[] parts = s3Url.replace("https://", "").split("/", 2);
            if (parts.length != 2) {
                return s3Url; // URL 형식이 맞지 않으면 원본 반환
            }

            String extractedBucket = parts[0].split("\\.")[0]; // bucket.s3... 에서 bucket 추출
            String key = parts[1];

            // GetObjectRequest 생성
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(extractedBucket)
                    .key(key)
                    .build();

            // Presigned URL 생성 (15분 유효)
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            // 에러 발생 시 원본 URL 반환
            return s3Url;
        }
    }
}


