package com.example.m6_thermal_power_plant_api.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean Cloudinary dùng chung cho mọi upload file/PDF (xem FileUploadService).
 *
 * Credentials đọc từ cloudinary.* trong application.properties — dev có default
 * hardcode, production inject qua biến môi trường CLOUDINARY_* (xem
 * application-prod.properties, cùng pattern JWT_SECRET/DB_PASSWORD).
 *
 * LƯU Ý cho account Cloudinary free/mới: mặc định Cloudinary CHẶN deliver file
 * PDF/ZIP — URL upload xong sẽ trả 401 khi mở. Phải bật một lần trong dashboard:
 * Settings -> Security -> "Allow delivery of PDF and ZIP files".
 */
@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.cloud-name}") String cloudName,
                                 @Value("${cloudinary.api-key}") String apiKey,
                                 @Value("${cloudinary.api-secret}") String apiSecret) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
