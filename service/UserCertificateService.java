package com.volunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.volunteer.dto.CertificateAuditDTO;
import com.volunteer.dto.UserCertificateDTO;
import com.volunteer.entity.UserCertificate;
import com.volunteer.vo.PageResult;

import java.util.List;

/**
 * 用户证书Service接口
 * 
 * @author volunteer-team
 * @since 2025-01-20
 */
public interface UserCertificateService extends IService<UserCertificate> {
    
    /**
     * 获取用户证书列表
     */
    List<UserCertificateDTO> getUserCertificates(Long userId);
    
    /**
     * 添加用户证书
     */
    boolean addCertificate(UserCertificate certificate);
    
    /**
     * 审核证书
     */
    boolean auditCertificate(CertificateAuditDTO auditDTO);
    
    /**
     * 获取待审核证书列表
     */
    List<UserCertificateDTO> getPendingCertificates();
    
    /**
     * 获取待审核证书列表（支持分页和搜索）
     */
    PageResult<UserCertificateDTO> getPendingCertificatesWithPage(
        Integer pageNo, Integer pageSize, String certificateName, 
        String userName, Long skillCategoryId, Integer status);
    
    /**
     * 获取证书详情
     */
    UserCertificateDTO getCertificateDetail(Long id);
    
    /**
     * 检查证书是否过期
     */
    List<UserCertificate> getExpiredCertificates();
}
