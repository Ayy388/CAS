package com.cas.modules.semester;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.modules.semester.dto.SemesterRequest;
import com.cas.modules.semester.dto.SemesterVO;

public interface SemesterService {

    Page<SemesterVO> listSemesters(int page, int pageSize);

    SemesterVO getSemesterById(Long id);

    SemesterVO createSemester(SemesterRequest request);

    SemesterVO updateSemester(Long id, SemesterRequest request);

    SemesterVO toggleActivate(Long id);

    void deleteSemester(Long id);
}