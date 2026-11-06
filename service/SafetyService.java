package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.EmergencyContact;
import com.volunteer.entity.EmergencyReport;

import java.util.List;

public interface SafetyService {

    // 紧急联系人
    List<EmergencyContact> listContacts(Long userId);
    boolean createContact(EmergencyContact contact);
    boolean updateContact(EmergencyContact contact);
    boolean deleteContact(Long id, Long userId);
    boolean setPrimary(Long id, Long userId);

    // 紧急上报
    boolean createReport(EmergencyReport report);
    EmergencyReport getReport(Long id, Long userId);
    Page<EmergencyReport> listMyReports(Long userId, int pageNo, int pageSize);
    boolean updateReportStatus(Long id, Integer status);
}


