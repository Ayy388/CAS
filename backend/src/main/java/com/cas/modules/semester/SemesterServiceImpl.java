package com.cas.modules.semester;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.exception.BusinessException;
import com.cas.modules.semester.dto.SemesterRequest;
import com.cas.modules.semester.dto.SemesterVO;
import com.cas.modules.semester.entity.Semester;
import com.cas.modules.semester.mapper.SemesterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {

    private final SemesterMapper semesterMapper;

    @Override
    public Page<SemesterVO> listSemesters(int page, int pageSize) {
        Page<Semester> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Semester> query = Wrappers.lambdaQuery();
        query.orderByDesc(Semester::getCreatedAt);
        Page<Semester> result = semesterMapper.selectPage(mpPage, query);

        Page<SemesterVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::toVO)
                .collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public SemesterVO getSemesterById(Long id) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException(404, "学期不存在");
        }
        return toVO(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SemesterVO createSemester(SemesterRequest request) {
        Semester semester = new Semester();
        semester.setName(request.getName());
        semester.setAcademicYear(request.getAcademicYear());
        semester.setSemesterType(request.getSemesterType());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setStatus("INACTIVE");
        semester.setCreatedAt(LocalDateTime.now());
        semester.setUpdatedAt(LocalDateTime.now());
        semesterMapper.insert(semester);
        return toVO(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SemesterVO updateSemester(Long id, SemesterRequest request) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException(404, "学期不存在");
        }
        semester.setName(request.getName());
        semester.setAcademicYear(request.getAcademicYear());
        semester.setSemesterType(request.getSemesterType());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setUpdatedAt(LocalDateTime.now());
        semesterMapper.updateById(semester);
        return toVO(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SemesterVO toggleActivate(Long id) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException(404, "学期不存在");
        }

        String currentStatus = semester.getStatus();

        if ("INACTIVE".equals(currentStatus)) {
            // Deactivate all other semesters first
            LambdaQueryWrapper<Semester> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Semester::getStatus, "ACTIVE");
            Semester inactive = new Semester();
            inactive.setStatus("INACTIVE");
            inactive.setUpdatedAt(LocalDateTime.now());
            semesterMapper.update(inactive, wrapper);

            // Activate the target
            semester.setStatus("ACTIVE");
        } else {
            // Toggle off
            semester.setStatus("INACTIVE");
        }

        semester.setUpdatedAt(LocalDateTime.now());
        semesterMapper.updateById(semester);
        return toVO(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSemester(Long id) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException(404, "学期不存在");
        }
        if ("ACTIVE".equals(semester.getStatus())) {
            throw new BusinessException(409, "不能删除已激活的学期");
        }
        semesterMapper.deleteById(id);
    }

    private SemesterVO toVO(Semester semester) {
        SemesterVO vo = new SemesterVO();
        vo.setId(semester.getId());
        vo.setName(semester.getName());
        vo.setAcademicYear(semester.getAcademicYear());
        vo.setSemesterType(semester.getSemesterType());
        vo.setStartDate(semester.getStartDate());
        vo.setEndDate(semester.getEndDate());
        vo.setStatus(semester.getStatus());
        return vo;
    }
}