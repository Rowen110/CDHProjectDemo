package com.cloudera.phoenixdemo.dao.outer;

import com.cloudera.phoenixdemo.entity.TagData;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagDataMapper {
    List<TagData> findTagDataByIds(@Param("tagDataIds")List<Integer> tagDataIds);
}