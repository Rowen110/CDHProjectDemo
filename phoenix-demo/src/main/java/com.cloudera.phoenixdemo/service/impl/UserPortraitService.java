package com.cloudera.phoenixdemo.service.impl;

import com.cloudera.phoenixdemo.dao.phoenix.UserPortraitMapper;
import com.cloudera.phoenixdemo.entity.BaseUserPortrait;
import com.cloudera.phoenixdemo.service.IUserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Charles
 * @package com.hdjt.bigdata.service.impl
 * @classname UserPortraitService
 * @description TODO
 * @date 2019-4-26 15:39
 */
@Service
public class UserPortraitService implements IUserPortraitService {
    @Autowired
    private UserPortraitMapper mapper;

    @Override
    public String upsertUserPortrait(BaseUserPortrait bean) {
        return mapper.upsertUserPortrait(bean);
    }
}
