package com.cloudera.phoenixdemo;

import com.cloudera.phoenixdemo.entity.PhoenixObject;
import com.cloudera.phoenixdemo.entity.PhoenixSchemaObject;
import com.cloudera.phoenixdemo.service.IPhoenixService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PhoenixDemoApplicationTests {

    @Autowired
    private IPhoenixService service;
    @Test
    public void contextLoads() {
    }
    @Test
    public void test() {
        List<PhoenixObject> list = service.getAll();
        List<PhoenixSchemaObject> list2 = service.getSchemaAll();
        System.out.println(list);
        System.out.println(list2);
    }

}
