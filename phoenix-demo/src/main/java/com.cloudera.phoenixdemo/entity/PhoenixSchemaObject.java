package com.cloudera.phoenixdemo.entity;

/**
 * @author Charles
 * @package com.eg.egsc.egc.osscomponent.mapper.entity
 * @classname PhoenixObject
 * @description TODO
 * @date 2019-4-22 15:19
 */
public class PhoenixSchemaObject {
    private String keys;
    private String f1;
    private String f2;
    private String s1;
    private String s2;

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public String getS1() {
        return s1;
    }

    public void setS1(String s1) {
        this.s1 = s1;
    }

    public String getS2() {
        return s2;
    }

    public void setS2(String s2) {
        this.s2 = s2;
    }

    @Override
    public String toString() {
        return "PhoenixSchemaObject{" +
                "keys='" + keys + '\'' +
                ", f1='" + f1 + '\'' +
                ", f2='" + f2 + '\'' +
                ", s1='" + s1 + '\'' +
                ", s2='" + s2 + '\'' +
                '}';
    }
}
