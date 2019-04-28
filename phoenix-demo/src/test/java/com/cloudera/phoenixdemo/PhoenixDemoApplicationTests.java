package com.cloudera.phoenixdemo;

import com.cloudera.phoenixdemo.entity.BaseUserPortrait;
import com.cloudera.phoenixdemo.entity.PhoenixObject;
import com.cloudera.phoenixdemo.entity.PhoenixSchemaObject;
import com.cloudera.phoenixdemo.service.IPhoenixService;
import com.cloudera.phoenixdemo.service.IUserPortraitService;
import com.cloudera.phoenixdemo.util.CityTypeUtil;
import com.cloudera.phoenixdemo.util.RandomLocalUtil;
import com.cloudera.phoenixdemo.util.RandomUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PhoenixDemoApplicationTests {

    @Autowired
    private IPhoenixService service1;
    @Autowired
    private IUserPortraitService service;

    @Test
    public void contextLoads() {
    }
    @Test
    public void test() {
        List<PhoenixObject> list = service1.getAll();
        List<PhoenixSchemaObject> list2 = service1.getSchemaAll();
        System.out.println(list);
        System.out.println(list2);
    }

    @Test
    public void generateDataToBaseUserPortrait() {

        RandomLocalUtil localutil = RandomLocalUtil.getInstance();
        BaseUserPortrait bean = new BaseUserPortrait();
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        List municipality = Arrays.asList("北京", "上海", "香港", "澳门", "台湾", "天津", "重庆");

        bean.setRowkey(uuid + String.format("%06d", 2));
        bean.setUserSex(String.valueOf(RandomUser.getNum(0, 2)));
        bean.setUserName(RandomUser.getChineseName(Integer.valueOf(bean.getUserSex())));
        bean.setUserAge(String.valueOf(RandomUser.getNum(8, 70)));
        bean.setUserCareer(String.valueOf(RandomUser.getNum(0, 6)));
        bean.setUserDegree(String.valueOf(RandomUser.getNum(0, 3)));
        bean.setIsMarried(String.valueOf(RandomUser.getNum(0, 2)));
        bean.setHasChrild(String.valueOf(RandomUser.getNum(0, 2)));
        bean.setFamilySum(String.valueOf(RandomUser.getNum(2, 10)));
        bean.setLocationProvince(localutil.getProvinces("中国").get(RandomUser.getNum(0, 33)));
        String locationProvince = bean.getLocationProvince();
        if (municipality.contains(locationProvince)) {
            bean.setLocationCity(locationProvince);
        } else {
            List<String> cities = localutil.getCities("中国", bean.getLocationProvince());
            bean.setLocationCity(cities.get(RandomUser.getNum(0, cities.size() - 1)));
        }
        bean.setCityType(CityTypeUtil.getCityType(bean.getLocationCity()));
//        bean.setCommunity(  community);
        bean.setFloor(String.valueOf(RandomUser.getNum(1, 33)));
        bean.setHouseType(Arrays.asList("一室", "两室", "三室", "四室", "五室", "五室以上").get(RandomUser.getNum(0, 5)));
        bean.setHouseArea(Arrays.asList("50.55", "78.66", "92.58", "108.46", "126.75", "189.99").get(RandomUser.getNum(0, 5)));
        bean.setFitmentType(Arrays.asList("毛坯", "简装", "精装修").get(RandomUser.getNum(0, 2)));
        bean.setEstateType(Arrays.asList("住宅", "别墅", "公寓", "商铺", "写字楼").get(RandomUser.getNum(0, 2)));
        bean.setPurchaseDate(CityTypeUtil.randomDate("2000-02-01", "2018-04-26"));
        bean.setPaymentType(Arrays.asList("一次性付款", "按揭借款付款").get(RandomUser.getNum(0, 1)));
        bean.setTotalPrices(String.valueOf(RandomUser.getNum(50, 1000)) + "万");
        bean.setDownPaymentPercent(String.valueOf(RandomUser.getNum(30, 100)) + "%");
        int i1 = Integer.valueOf(bean.getTotalPrices().substring(0, bean.getTotalPrices().length() - 1));
        int i2 = Integer.valueOf(bean.getDownPaymentPercent().substring(0, bean.getDownPaymentPercent().length() - 1));
        bean.setDownPaymentPrices(String.valueOf(i1 * i2 / 100) + "万");
        bean.setLoansType(Arrays.asList("公积金贷款", "商业贷款", "抵押贷款").get(RandomUser.getNum(0, 2)));
        bean.setHasCar(String.valueOf(RandomUser.getNum(0, 2)));
        bean.setHasCarport(String.valueOf(RandomUser.getNum(0, 2)));

        System.out.println(bean);
        String s = service.upsertUserPortrait(bean);
        System.out.println("============================" + s + "=================================");
    }

}
