package com.cas.modules.course;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cas.common.result.ApiResponse;
import com.cas.common.result.PageResponse;
import com.cas.modules.course.dto.CourseRequest;
import com.cas.modules.course.dto.CourseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<PageResponse<CourseVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        Page<CourseVO> result = courseService.listCourses(page, pageSize, keyword, type);
        return ApiResponse.success(PageResponse.of(result));
    }

    @PostMapping
    public ApiResponse<CourseVO> create(@Valid @RequestBody CourseRequest request) {
        CourseVO vo = courseService.createCourse(request);
        return ApiResponse.success(vo);
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseVO> update(@PathVariable Long id,
                                        @Valid @RequestBody CourseRequest request) {
        CourseVO vo = courseService.updateCourse(id, request);
        return ApiResponse.success(vo);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResponse.success();
    }
}