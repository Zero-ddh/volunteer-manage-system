package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.volunteer.entity.RecruitmentAdmission;
import com.volunteer.entity.RecruitmentInfo;
import com.volunteer.exception.BusinessException;
import com.volunteer.mapper.RecruitmentAdmissionMapper;
import com.volunteer.mapper.RecruitmentInfoMapper;
import com.volunteer.service.MessageService;
import com.volunteer.service.RecruitmentService;
import com.volunteer.vo.RecruitmentAdmissionVO;
import com.volunteer.vo.RecruitmentApplicantVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class RecruitmentServiceImpl extends ServiceImpl<RecruitmentInfoMapper, RecruitmentInfo> implements RecruitmentService {

    @Resource
    private RecruitmentInfoMapper recruitmentInfoMapper;
    @Resource
    private RecruitmentAdmissionMapper recruitmentAdmissionMapper;
    @Resource
    private MessageService messageService;

    @Override
    public RecruitmentInfo create(RecruitmentInfo info) {
        recruitmentInfoMapper.insert(info);
        return info;
    }

    @Override
    public boolean updateByIdSafe(Long id, RecruitmentInfo info) {
        info.setId(id);
        return recruitmentInfoMapper.updateById(info) > 0;
    }

    @Override
    public Page<RecruitmentInfo> pageList(String keyword, Integer status, int pageNo, int pageSize) {
        Page<RecruitmentInfo> page = new Page<>(pageNo, pageSize);
        return recruitmentInfoMapper.selectRecruitmentPageWithApplicantCount(page, keyword, status);
    }

    @Override
    public RecruitmentInfo findDetail(Long id) {
        return recruitmentInfoMapper.selectById(id);
    }

    @Override
    public boolean apply(Long recruitmentId, Long userId, String intro, String skills, String experience) {
        // 检查是否已经申请过
        LambdaQueryWrapper<RecruitmentAdmission> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(RecruitmentAdmission::getRecruitmentId, recruitmentId)
                   .eq(RecruitmentAdmission::getUserId, userId)
                   .eq(RecruitmentAdmission::getDeleted, 0);
        
        RecruitmentAdmission existing = recruitmentAdmissionMapper.selectOne(checkWrapper);
        if (existing != null) {
            throw new BusinessException(400, "您已经申请过此招募，不能重复申请");
        }
        
        RecruitmentAdmission admission = new RecruitmentAdmission();
        admission.setRecruitmentId(recruitmentId);
        admission.setUserId(userId);
        admission.setStatus(1); // 待审核
        admission.setRemark(intro);
        admission.setSkills(skills);
        admission.setExperience(experience);
        admission.setDeleted(0);
        return recruitmentAdmissionMapper.insert(admission) > 0;
    }

    @Override
    public Page<RecruitmentAdmission> listApplicants(Long recruitmentId, int pageNo, int pageSize) {
        Page<RecruitmentAdmission> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<RecruitmentAdmission> qw = new LambdaQueryWrapper<>();
        qw.eq(RecruitmentAdmission::getRecruitmentId, recruitmentId)
          .eq(RecruitmentAdmission::getDeleted, 0)
          .orderByDesc(RecruitmentAdmission::getCreatedTime);
        return recruitmentAdmissionMapper.selectPage(page, qw);
    }

    @Override
    public Page<RecruitmentApplicantVO> listApplicantsWithUserInfo(Long recruitmentId, String name, String phone, Integer status, int pageNo, int pageSize) {
        Page<RecruitmentApplicantVO> page = new Page<>(pageNo, pageSize);
        return recruitmentAdmissionMapper.selectApplicantsWithUserInfo(page, recruitmentId, name, phone, status);
    }

    @Override
    public Page<RecruitmentAdmission> getUserApplications(Long userId, int pageNo, int pageSize) {
        Page<RecruitmentAdmission> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<RecruitmentAdmission> qw = new LambdaQueryWrapper<>();
        qw.eq(RecruitmentAdmission::getUserId, userId)
          .eq(RecruitmentAdmission::getDeleted, 0)
          .orderByDesc(RecruitmentAdmission::getCreatedTime);
        return recruitmentAdmissionMapper.selectPage(page, qw);
    }

    @Override
    public Page<RecruitmentAdmissionVO> getUserApplicationsWithDetails(Long userId, int pageNo, int pageSize) {
        Page<RecruitmentAdmission> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<RecruitmentAdmission> qw = new LambdaQueryWrapper<>();
        qw.eq(RecruitmentAdmission::getUserId, userId)
          .eq(RecruitmentAdmission::getDeleted, 0)
          .orderByDesc(RecruitmentAdmission::getCreatedTime);
        
        Page<RecruitmentAdmission> admissionPage = recruitmentAdmissionMapper.selectPage(page, qw);
        
        // 转换为VO
        Page<RecruitmentAdmissionVO> voPage = new Page<>(pageNo, pageSize);
        voPage.setTotal(admissionPage.getTotal());
        voPage.setPages(admissionPage.getPages());
        
        List<RecruitmentAdmissionVO> voList = admissionPage.getRecords().stream().map(admission -> {
            RecruitmentAdmissionVO vo = new RecruitmentAdmissionVO();
            vo.setId(admission.getId());
            vo.setRecruitmentId(admission.getRecruitmentId());
            vo.setUserId(admission.getUserId());
            vo.setStatus(admission.getStatus() - 1); // 转换状态：1->0, 2->1, 3->2
            vo.setIntro(admission.getRemark());
            vo.setSkills(admission.getSkills());
            vo.setExperience(admission.getExperience());
            vo.setApplyTime(admission.getCreatedTime());
            vo.setReviewTime(admission.getStatus() != 1 ? admission.getReviewTime() : null);
            vo.setReviewRemark(admission.getReviewRemark());
            vo.setCreatedTime(admission.getCreatedTime());
            vo.setUpdatedTime(admission.getUpdatedTime());
            
            // 获取招募标题
            RecruitmentInfo recruitment = recruitmentInfoMapper.selectById(admission.getRecruitmentId());
            if (recruitment != null) {
                vo.setRecruitmentTitle(recruitment.getTitle());
            }
            
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public boolean reviewApplication(Long admissionId, Integer status, String remark, Long reviewerId) {
        RecruitmentAdmission admission = recruitmentAdmissionMapper.selectById(admissionId);
        if (admission == null) {
            throw new BusinessException(400, "申请记录不存在");
        }
        
        // 更新状态和审核信息
        admission.setStatus(status);
        admission.setReviewRemark(remark);
        admission.setReviewTime(java.time.LocalDateTime.now());
        
        boolean result = recruitmentAdmissionMapper.updateById(admission) > 0;
        
        if (result) {
            // 发送审核结果通知
            sendReviewNotification(admission, status, remark);
        }
        
        return result;
    }
    
    /**
     * 发送审核结果通知
     */
    private void sendReviewNotification(RecruitmentAdmission admission, Integer status, String remark) {
        try {
            // 获取招募信息
            RecruitmentInfo recruitment = recruitmentInfoMapper.selectById(admission.getRecruitmentId());
            
            String title, content;
            if (status == 2) { // 通过
                title = "🎉 恭喜！您的招募申请已通过";
                content = String.format(
                    "亲爱的志愿者，\n\n" +
                    "恭喜您！您申请的招募「%s」已通过审核。\n\n" +
                    "审核备注：%s\n\n" +
                    "请及时关注后续安排，感谢您的参与！\n\n" +
                    "志愿者服务平台",
                    recruitment.getTitle(),
                    remark != null ? remark : "无"
                );
            } else { // 拒绝
                title = "📝 招募申请审核结果通知";
                content = String.format(
                    "亲爱的志愿者，\n\n" +
                    "很抱歉，您申请的招募「%s」未通过审核。\n\n" +
                    "审核备注：%s\n\n" +
                    "请不要气馁，我们期待您继续关注其他招募机会。\n\n" +
                    "志愿者服务平台",
                    recruitment.getTitle(),
                    remark != null ? remark : "无"
                );
            }
            
            // 发送消息
            messageService.sendMessage(
                admission.getUserId(),
                1, // 招募审核结果
                title,
                content,
                admission.getRecruitmentId(),
                "recruitment"
            );
        } catch (Exception e) {
            log.error("发送审核通知失败", e);
        }
    }
}


