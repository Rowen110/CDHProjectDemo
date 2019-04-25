package com.cloudera.phoenixdemo.service;



import com.cloudera.phoenixdemo.entity.PhoenixObject;
import com.cloudera.phoenixdemo.entity.PhoenixSchemaObject;

import java.util.List;

public interface IPhoenixService {
    List<PhoenixObject> getAll();
    List<PhoenixSchemaObject> getSchemaAll();
}
