package com.cloudera.phoenixdemo.service;

import com.cloudera.phoenixdemo.entity.BaseUserPortrait;

public interface IUserPortraitService {
    String upsertUserPortrait(BaseUserPortrait bean);
}
