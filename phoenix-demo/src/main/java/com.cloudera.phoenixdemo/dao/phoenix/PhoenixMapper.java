package com.cloudera.phoenixdemo.dao.phoenix;

import com.cloudera.phoenixdemo.entity.PhoenixObject;
import com.cloudera.phoenixdemo.entity.PhoenixSchemaObject;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PhoenixMapper {
    List<PhoenixObject> getAll();
    List<PhoenixSchemaObject> getSchemaAll();
}
