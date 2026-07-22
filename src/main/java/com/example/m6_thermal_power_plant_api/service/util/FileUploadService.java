package com.example.m6_thermal_power_plant_api.service.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.m6_thermal_power_plant_api.dto.file.FileUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Upload/xoá file trên Cloudinary — hạ tầng DÙNG CHUNG cho mọi nghiệp vụ
 * (phiếu công tác PDF, ảnh đánh giá kỹ thuật, ảnh thiết bị...).
 *
 * Kế thừa pattern từ project office_rental_md5 nhưng sửa 2 bug của bản cũ:
 *  1. resource_type upload/xoá lệch nhau ("image" vs "raw") → xoá fail âm thầm.
 *     Fix: {@link FileUploadResult} trả về resourceType, caller lưu lại và truyền
 *     đúng giá trị đó cho {@link #deleteFile} — nhất quán by-construction.
 *  2. extractPublicIdFromUrl parse ngược URL, cắt mất folder prefix → destroy sai
 *     target. Fix: KHÔNG còn hàm đó — caller lưu publicId từ FileUploadResult,
 *     không bao giờ parse URL.
 *
 * KHÔNG dùng cho việc ghi file vào src/main/resources (cách TechnicalAssessmentService
 * đang làm) — thư mục src/ không tồn tại khi chạy jar production và file mất khi
 * redeploy container.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;

    /**
     * Upload ảnh (MultipartFile từ client). Chỉ nhận content-type image/*.
     *
     * @throws IllegalArgumentException file rỗng hoặc không phải ảnh
     */
    public FileUploadResult uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File anh khong duoc rong.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "Chi chap nhan file anh (image/*), nhan duoc: " + contentType);
        }

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return toResult(result);
    }

    public List<FileUploadResult> uploadImages(
            MultipartFile[] files
    ) throws IOException {

        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        List<FileUploadResult> results = new ArrayList<>();

        for (MultipartFile file : files) {
            results.add(uploadImage(file));
        }

        return results;
    }

    /**
     * Upload PDF đã sinh sẵn ở backend (VD: từ PDFService.renderPdf).
     *
     * Upload với resource_type "image" + format "pdf" (giống bản cũ) vì Cloudinary
     * coi PDF là image asset: sau này sinh được thumbnail từng trang qua
     * transformation nếu cần. Muốn xoá thì truyền lại đúng resourceType/publicId
     * từ {@link FileUploadResult} đã lưu.
     *
     * @param fileBytes nội dung PDF
     * @param folder    thư mục trên Cloudinary, VD "work-orders" (không có "/" cuối)
     * @param fileName  tên file KHÔNG đuôi, VD orderCode "WO-260706120000-001"
     */
    public FileUploadResult uploadPdf(byte[] fileBytes, String folder, String fileName) throws IOException {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("Noi dung PDF khong duoc rong.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName la bat buoc.");
        }
        String publicId = (folder == null || folder.isBlank())
                ? fileName
                : folder + "/" + fileName;

        Map<?, ?> result = cloudinary.uploader().upload(fileBytes, ObjectUtils.asMap(
                "resource_type", "image",
                "format", "pdf",
                "public_id", publicId,
                "overwrite", true
        ));
        return toResult(result);
    }

    /**
     * Xoá asset trên Cloudinary. {@code publicId} và {@code resourceType} lấy từ
     * {@link FileUploadResult} đã lưu lúc upload — KHÔNG parse từ URL.
     *
     * @throws IllegalStateException Cloudinary không tìm thấy asset (sai
     *                               publicId/resourceType) — fail to, không im lặng
     */
    public void deleteFile(String publicId, String resourceType) throws IOException {
        if (publicId == null || publicId.isBlank()) {
            return; // chưa từng upload — không có gì để xoá
        }

        Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                "resource_type", resourceType == null ? "image" : resourceType,
                "invalidate", true
        ));

        String outcome = String.valueOf(result.get("result"));
        if (!"ok".equals(outcome)) {
            // "not found" = sai publicId hoặc sai resourceType — đây chính là lỗi
            // bản cũ nuốt mất; ném ra để caller biết dữ liệu đang lệch.
            throw new IllegalStateException(
                    "Xoa file Cloudinary that bai (publicId=" + publicId
                            + ", resourceType=" + resourceType + "): " + outcome);
        }
        log.info("Da xoa file Cloudinary: {}", publicId);
    }

    /**
     * Trich xuat public ID tu URL Cloudinary.
     * Ho tro URL dang:
     * https://res.cloudinary.com/cloud_name/image/upload/v12345678/folder/subfolder/filename.jpg
     */
    public static String extractPublicIdFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx == -1) {
            uploadIdx = url.indexOf("/private/");
            if (uploadIdx == -1) {
                uploadIdx = url.indexOf("/authenticated/");
                if (uploadIdx == -1) {
                    return null;
                }
            }
        }
        
        // Cat chuoi tu sau "/upload/" hoac "/private/" hoac "/authenticated/"
        // Vi du: v1720846513/consumables/mo-bo-tron.jpg
        String path = url.substring(url.indexOf("/", uploadIdx + 1) + 1);
        
        // Loai bo version prefix neu co (vi du: v1720846513/)
        if (path.matches("^v\\d+/.*")) {
            path = path.substring(path.indexOf("/") + 1);
        }
        
        // Loai bo duoi mo rong file (.jpg, .png, ...)
        int dotIdx = path.lastIndexOf(".");
        if (dotIdx != -1) {
            path = path.substring(0, dotIdx);
        }
        
        return path;
    }

    private static FileUploadResult toResult(Map<?, ?> uploadResult) {
        Object bytes = uploadResult.get("bytes");
        return new FileUploadResult(
                String.valueOf(uploadResult.get("public_id")),
                String.valueOf(uploadResult.get("secure_url")),
                String.valueOf(uploadResult.get("resource_type")),
                uploadResult.get("format") != null ? String.valueOf(uploadResult.get("format")) : null,
                bytes instanceof Number n ? n.longValue() : 0L
        );
    }
}
