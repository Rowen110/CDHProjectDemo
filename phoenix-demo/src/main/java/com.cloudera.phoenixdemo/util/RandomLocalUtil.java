package com.cloudera.phoenixdemo.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Charles
 * @package com.hdjt.bigdata.util
 * @classname RandomLocal
 * @description TODO
 * @date 2019-4-26 12:02
 */
public class RandomLocalUtil {
    //各地区xml文件路径
    private static final String LOCAL_LIST_PATH = "config/LocationList.xml";
    //所有国家名称List
    private static final List<String> COUNTRY_REGION = new ArrayList<String>();
    private static RandomLocalUtil localutil;
    private SAXReader reader;
    private Document document;
    //根元素
    private Element rootElement;

    //初始化
    private RandomLocalUtil() {
        //1.读取
        reader = new SAXReader();
        try {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(LOCAL_LIST_PATH);
            document = reader.read(in);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        //2.获得根元素
        rootElement = document.getRootElement();
        //3.初始化所有国家名称列表
        Iterator it = rootElement.elementIterator();
        Element ele = null;
        while (it.hasNext()) {
            ele = (Element) it.next();
            COUNTRY_REGION.add(ele.attributeValue("Name"));
        }
    }

    /**
     * @author LiuJinan
     * @TODO 功能：	获取所有国家名称
     * @time 2016-8-26 上午9:02:05
     * @return List<String>
     */
    public List<String> getCountry() {
        return COUNTRY_REGION;
    }

    /**
     * @param countryName 国家名，从getCountry()从取出
     * @author LiuJinan
     * @TODO 功能：	根据国家名获取该国所有省份
     * @time 2016-8-26 上午9:07:21
     * @return List<Element>
     */
    private List<Element> provinces(String countryName) {
        Iterator it = rootElement.elementIterator();
        List<Element> provinces = new ArrayList<Element>();
        Element ele = null;
        while (it.hasNext()) {
            ele = (Element) it.next();
            COUNTRY_REGION.add(ele.attributeValue("Name"));
            if (ele.attributeValue("Name").equals(countryName)) {
                provinces = ele.elements();
                break;
            }
        }
        return provinces;
    }

    /**
     * @param countryName 国家名，从getCountry()从取出
     * @author LiuJinan
     * @TODO 功能：	根据国家名获取该国所有省份
     * @time 2016-8-26 上午9:07:21
     * @return List<String>
     */
    public List<String> getProvinces(String countryName) {
        List<Element> tmp = this.provinces(countryName);
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < tmp.size(); i++) {
            list.add(tmp.get(i).attributeValue("Name"));
        }
        return list;
    }

    /**
     * @param provinceName
     * @return
     * @author LiuJinan
     * @TODO 功能：根据国家名和省份名，获取该省城市名列表
     * @time 2016-8-26 上午9:15:24
     */
    private List<Element> cities(String countryName, String provinceName) {
        List<Element> provinces = this.provinces(countryName);
        List<Element> cities = new ArrayList<Element>();
        if (provinces == null || provinces.size() == 0) {        //没有这个城市
            return cities;
        }

        for (int i = 0; i < provinces.size(); i++) {
            if (provinces.get(i).attributeValue("Name").equals(provinceName)) {
                cities = provinces.get(i).elements();
                break;
            }
        }
        return cities;
    }

    /**
     * @param countryName
     * @param provinceName
     * @author LiuJinan
     * @TODO 功能：根据国家名和省份名获取城市列表
     * @time 2016-8-26 下午4:55:55
     * @return List<String>
     */
    public List<String> getCities(String countryName, String provinceName) {
        List<Element> tmp = this.cities(countryName, provinceName);
        List<String> cities = new ArrayList<String>();
        for (int i = 0; i < tmp.size(); i++) {
            cities.add(tmp.get(i).attributeValue("Name"));
        }
        return cities;
    }

    public static RandomLocalUtil getInstance() {
        if (localutil == null) {
            localutil = new RandomLocalUtil();
        }
        return localutil;
    }
    public static void main(String[] args) {
//        RandomLocalUtil localutil =  RandomLocalUtil.getInstance();
//        List<String> list = localutil.getProvinces("中国");
////        List<String> list = localutil.getCities("中国", "广东");
//        for(int i=0; i<list.size(); i++){
//            System.out.print(list.get(i) + " ");
//        }

    }
}
