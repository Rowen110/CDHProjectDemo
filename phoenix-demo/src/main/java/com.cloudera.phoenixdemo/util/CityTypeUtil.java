package com.cloudera.phoenixdemo.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Charles
 * @package com.hdjt.bigdata.util
 * @classname CityType
 * @description TODO
 * @date 2019-4-26 14:12
 */
public class CityTypeUtil {

    private static List list1 = Arrays.asList("北京", "上海", "广州", "深圳", "香港", "澳门", "台湾");
    private static List list2 = Arrays.asList("天津", "南京", "苏州", "杭州", "青岛", "烟台", "西安", "无锡", "三亚", "大连", "武汉", "成都", "重庆", "长沙", "厦门", "沈阳");
    private static List list3 = Arrays.asList(
            "石家庄", "唐山", "秦皇岛", "邯郸", "衡水", "太原", "运城", "呼和浩特", "鄂尔多斯",
            "呼伦贝尔", "铁岭", "锦州", "长春", "吉林", "哈尔滨", "齐齐哈尔", "徐州", "常州", "苏州",
            "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁", "绍兴", "宁波", "舟山",
            "金华", "温州", "芜湖", "福州", "莆田", "泉州"
    );
    private static List list4 = Arrays.asList(
            "邢台", "保定", "张家口", "临汾",
            "晋中", "大同", "赤峰", "乌兰察布", "兴安", "抚顺",
            "葫芦岛", "白山", "松原", "白城",
            "牡丹江", "黑河", "绥化", "大兴安岭", "嘉兴", "湖州",
            "衢州", "台州", "丽水", "黄山", "安庆", "漳州", "南平", "龙岩", "宁德"
    );

    public static String getCityType(String city) {
        String cityType = "";
        int num = RandomUser.getNum(3, 5);
        boolean contains = list1.contains(city);
        if (list1.contains(city)) {
            cityType = "一线";
        } else if (list2.contains(city)) {
            cityType = "二线";
        } else if (list3.contains(city)) {
            cityType = "三线";
        } else if (list4.contains(city)) {
            cityType = "四线";
        } else {
            cityType = "五线";
        }
        return cityType;
    }

    public static String randomDate(String beginDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date start = format.parse(beginDate);// 构造开始日期
            Date end = format.parse(endDate);// 构造结束日期
            // getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
            if (start.getTime() >= end.getTime()) {
                return null;
            }
            long date = random(start.getTime(), end.getTime());
            return format.format(new Date(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long random(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        // 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
        if (rtn == begin || rtn == end) {
            return random(begin, end);
        }
        return rtn;
    }

    public static void main(String[] args) {
//        List<String> provinces = RandomLocalUtil.getInstance().getProvinces("中国");
//        for (String pro : provinces) {
//            System.out.println("===========================" + pro + "===========================");
//            List<String> cities = RandomLocalUtil.getInstance().getCities("中国", pro);
//            for (String city : cities) {
//                System.out.println(city);
//            }
//        }
    }
}
