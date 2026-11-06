package com.volunteer.service;

/**
 * 二维码服务接口
 * 
 * @author volunteer-team
 * @since 2025-01-21
 */
public interface QRCodeService {
    
    /**
     * 生成二维码图片（Base64编码）
     * 
     * @param qrCode 二维码内容
     * @return Base64编码的图片数据
     */
    String generateQRCodeImage(String qrCode);
}
