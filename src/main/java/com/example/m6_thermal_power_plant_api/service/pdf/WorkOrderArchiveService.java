package com.example.m6_thermal_power_plant_api.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ĐÓNG BĂNG bản lưu PDF khi phiếu công tác về trạng thái kết thúc
 * (COMPLETED/CANCELLED): chốt sổ cả PDF phiếu công tác (pdf_path) lẫn PDF
 * phiếu cấp vật tư (supplies_pdf_path) đúng MỘT lần — sau thời điểm này các
 * lần xuất PDF chỉ render bytes, không upload đè bản lưu nữa (bản lưu phải
 * khớp bản giấy đã ký, không được trôi).
 *
 * KHÔNG BAO GIỜ ném exception: đổi trạng thái phiếu là nghiệp vụ chính, không
 * được thất bại vì render/mạng/Cloudinary — lỗi chỉ log cảnh báo, lần xuất PDF
 * thủ công sau đó của phiếu (đã terminal, pdf_path còn null) sẽ tự lưu bù.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderArchiveService {

    private final WorkOrderPdfService workOrderPdfService;
    private final SuppliesIssuePdfService suppliesIssuePdfService;

    public void archiveOnClose(Integer workOrderId) {
        try {
            workOrderPdfService.archive(workOrderId);
        } catch (Exception e) {
            log.warn("Khong luu duoc ban dong bang PDF phieu cong tac id {} — se luu bu o lan xuat PDF sau.",
                    workOrderId, e);
        }
        try {
            suppliesIssuePdfService.archive(workOrderId);
        } catch (Exception e) {
            log.warn("Khong luu duoc ban dong bang PDF phieu cap vat tu cua PCT id {}.", workOrderId, e);
        }
    }
}
