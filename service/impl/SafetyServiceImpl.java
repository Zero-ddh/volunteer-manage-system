package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.EmergencyContact;
import com.volunteer.entity.EmergencyReport;
import com.volunteer.mapper.EmergencyContactMapper;
import com.volunteer.mapper.EmergencyReportMapper;
import com.volunteer.service.SafetyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SafetyServiceImpl implements SafetyService {

    @Resource
    private EmergencyContactMapper emergencyContactMapper;

    @Resource
    private EmergencyReportMapper emergencyReportMapper;

    @Override
    public List<EmergencyContact> listContacts(Long userId) {
        return emergencyContactMapper.selectList(new LambdaQueryWrapper<EmergencyContact>()
                .eq(EmergencyContact::getUserId, userId)
                .orderByDesc(EmergencyContact::getIsPrimary)
                .orderByDesc(EmergencyContact::getUpdatedTime));
    }

    @Override
    public boolean createContact(EmergencyContact contact) {
        return emergencyContactMapper.insert(contact) > 0;
    }

    @Override
    public boolean updateContact(EmergencyContact contact) {
        return emergencyContactMapper.updateById(contact) > 0;
    }

    @Override
    public boolean deleteContact(Long id, Long userId) {
        return emergencyContactMapper.delete(new LambdaQueryWrapper<EmergencyContact>()
                .eq(EmergencyContact::getId, id)
                .eq(EmergencyContact::getUserId, userId)) > 0;
    }

    @Override
    public boolean setPrimary(Long id, Long userId) {
        // 取消当前主联系人
        EmergencyContact toPrimary = emergencyContactMapper.selectOne(new LambdaQueryWrapper<EmergencyContact>()
                .eq(EmergencyContact::getId, id)
                .eq(EmergencyContact::getUserId, userId));
        if (toPrimary == null) {
            return false;
        }
        // 将该用户的其他主联系人置为0
        List<EmergencyContact> contacts = listContacts(userId);
        for (EmergencyContact c : contacts) {
            if (c.getIsPrimary() != null && c.getIsPrimary() == 1 && !c.getId().equals(id)) {
                c.setIsPrimary(0);
                emergencyContactMapper.updateById(c);
            }
        }
        toPrimary.setIsPrimary(1);
        return emergencyContactMapper.updateById(toPrimary) > 0;
    }

    @Override
    public boolean createReport(EmergencyReport report) {
        report.setStatus(1);
        return emergencyReportMapper.insert(report) > 0;
    }

    @Override
    public EmergencyReport getReport(Long id, Long userId) {
        return emergencyReportMapper.selectOne(new LambdaQueryWrapper<EmergencyReport>()
                .eq(EmergencyReport::getId, id)
                .eq(EmergencyReport::getReporterUserId, userId));
    }

    @Override
    public Page<EmergencyReport> listMyReports(Long userId, int pageNo, int pageSize) {
        Page<EmergencyReport> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<EmergencyReport> wrapper = new LambdaQueryWrapper<EmergencyReport>()
                .eq(EmergencyReport::getReporterUserId, userId)
                .orderByDesc(EmergencyReport::getCreatedTime);
        return emergencyReportMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean updateReportStatus(Long id, Integer status) {
        EmergencyReport exist = emergencyReportMapper.selectById(id);
        if (exist == null) {
            return false;
        }
        exist.setStatus(status);
        return emergencyReportMapper.updateById(exist) > 0;
    }
}


