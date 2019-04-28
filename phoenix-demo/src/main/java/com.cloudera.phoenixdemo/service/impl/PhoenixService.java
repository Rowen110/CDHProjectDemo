package com.cloudera.phoenixdemo.service.impl;

import com.cloudera.phoenixdemo.dao.phoenix.PhoenixMapper;
import com.cloudera.phoenixdemo.entity.PhoenixObject;
import com.cloudera.phoenixdemo.entity.PhoenixSchemaObject;
import com.cloudera.phoenixdemo.service.IPhoenixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Charles
 * @package com.example.phoenixdemo.service.impl
 * @classname PhoenixService
 * @description TODO
 * @date 2019-4-22 19:08
 */
@Service
public class PhoenixService implements IPhoenixService {

    @Autowired
    private PhoenixMapper mapper;

    @Override
    public List<PhoenixObject> getAll() {
        return mapper.getAll();
    }

    @Override
    public List<PhoenixSchemaObject> getSchemaAll() {
        return mapper.getSchemaAll();
    }
}
