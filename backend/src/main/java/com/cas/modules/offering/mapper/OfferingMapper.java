package com.cas.modules.offering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cas.modules.offering.entity.Offering;
import org.apache.ibatis.annotations.Update;

public interface OfferingMapper extends BaseMapper<Offering> {

    @Update("UPDATE course_offering SET enrolled_count = enrolled_count + 1 " +
            "WHERE id = #{offeringId} AND enrolled_count < #{maxCapacity}")
    int updateEnrolledCount(Long offeringId, Integer maxCapacity);

    @Update("UPDATE course_offering SET enrolled_count = enrolled_count - 1 " +
            "WHERE id = #{offeringId} AND enrolled_count > 0")
    int decrementEnrolledCount(Long offeringId);
}