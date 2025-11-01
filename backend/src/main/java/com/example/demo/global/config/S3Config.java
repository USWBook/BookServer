package com.example.demo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${custom.aws.s3.bucket}")
    private String bucket;

    // Local/Dev 환경: AccessKey 사용
    @Bean
    @Profile({"local", "dev"})
    public S3Client s3ClientLocal(
            @Value("${custom.aws.credentials.access-key}") String accessKey,
            @Value("${custom.aws.credentials.secret-key}") String secretKey
    ) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    // STG/Prod 환경: IAM Role 사용
    @Bean
    @Profile({"stg", "prod"})
    public S3Client s3ClientCloud() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public String s3Bucket() {
        return bucket;
    }
}







