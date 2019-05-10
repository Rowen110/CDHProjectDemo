package com.cloudera.phoenixdemo.dao.phoenix;

import com.cloudera.phoenixdemo.entity.BaseUserPortrait;
import com.cloudera.phoenixdemo.entity.TagData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPortraitMapper {
    int upsertUserPortrait(BaseUserPortrait bean);

    List<BaseUserPortrait> getBaseUserByTagDataId(@Param("tagDataList") List<TagData> tagDataList);
}
