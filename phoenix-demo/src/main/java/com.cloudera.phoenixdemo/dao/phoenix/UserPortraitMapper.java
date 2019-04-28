package com.cloudera.phoenixdemo.dao.phoenix;

import com.cloudera.phoenixdemo.entity.BaseUserPortrait;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPortraitMapper {
    String upsertUserPortrait(BaseUserPortrait bean);
}
