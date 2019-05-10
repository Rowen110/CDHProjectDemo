package com.cloudera.phoenixdemo.service;

import com.cloudera.phoenixdemo.entity.BaseUserPortrait;
import com.cloudera.phoenixdemo.entity.PageModel;

public interface IUserPortraitService {
    int upsertUserPortrait(BaseUserPortrait bean);

    PageModel<BaseUserPortrait> getBaseUserByTagDataId(PageModel pageModel);
}
