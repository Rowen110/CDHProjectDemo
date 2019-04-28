package com.cloudera.phoenixdemo.entity;

/**
 * @author Charles
 * @package com.hdjt.bigdata
 * @classname BaseUserPortrait
 * @description TODO
 * @date 2019-4-26 10:36
 */
public class BaseUserPortrait {
    
    private String rowkey;
    //姓名
    private String userName;
    //性别	男 1、女 0、未知 2
    private String userSex;
    //年龄	18岁以下，18-25,25-35,35-45，45-55,55以上
    private String userAge;
    //职业	公务员 0、职员 1、教师 2、医生 3 等
    private String userCareer;
    //学历	初中及以下 0、高中 1、本科 2、研究生及以上 3
    private String userDegree;
    //婚姻状况	已婚 1、未婚 0、未知 2
    private String isMarried;
    //是否有小孩	有 1、无0、未知 2
    private String hasChrild;
    //家庭人数
    private String familySum;
    //所在省份
    private String locationProvince;
    //所在城市
    private String locationCity;
    //城市等级	一线、二线、三线、四线、五线
    private String cityType;
    //小区
    private String community;
    //楼层
    private String floor;
    //房型	一室、两室、三室、四室、五室、五室以上
    private String houseType;
    //面积	50平以下，50-70平，70-90平，90-110平，110-140平，140-170平，170-200平，200平以上
    private String houseArea;
    //装修类型	毛培、简装、精装修
    private String fitmentType;
    //物业类型	住宅、别墅、公寓、商铺、写字楼
    private String estateType;
    //购买日期
    private String purchaseDate;
    //付款方式	一次性付款、按揭借款付款
    private String paymentType;
    //总价
    private String totalPrices;
    //首付比例
    private String downPaymentPercent;
    //首付金额
    private String downPaymentPrices;
    //贷款方式	公积金贷款、商业贷款、抵押贷款
    private String loansType;
    //有否有车	有、无、未知
    private String hasCar;
    //是否有车位	有
    private String hasCarport;

    @Override
    public String toString() {
        return "BaseUserPortrait{" +
                "rowkey='" + rowkey + '\'' +
                ", userName='" + userName + '\'' +
                ", userSex='" + userSex + '\'' +
                ", userAge='" + userAge + '\'' +
                ", userCareer='" + userCareer + '\'' +
                ", userDegree='" + userDegree + '\'' +
                ", isMarried='" + isMarried + '\'' +
                ", hasChrild='" + hasChrild + '\'' +
                ", familySum='" + familySum + '\'' +
                ", locationProvince='" + locationProvince + '\'' +
                ", locationCity='" + locationCity + '\'' +
                ", cityType='" + cityType + '\'' +
                ", community='" + community + '\'' +
                ", floor='" + floor + '\'' +
                ", houseType='" + houseType + '\'' +
                ", houseArea='" + houseArea + '\'' +
                ", fitmentType='" + fitmentType + '\'' +
                ", estateType='" + estateType + '\'' +
                ", purchaseDate='" + purchaseDate + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", totalPrices='" + totalPrices + '\'' +
                ", downPaymentPercent='" + downPaymentPercent + '\'' +
                ", downPaymentPrices='" + downPaymentPrices + '\'' +
                ", loansType='" + loansType + '\'' +
                ", hasCar='" + hasCar + '\'' +
                ", hasCarport='" + hasCarport + '\'' +
                '}';
    }

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserSex() {
        return userSex;
    }

    public void setUserSex(String userSex) {
        this.userSex = userSex;
    }

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }

    public String getUserCareer() {
        return userCareer;
    }

    public void setUserCareer(String userCareer) {
        this.userCareer = userCareer;
    }

    public String getUserDegree() {
        return userDegree;
    }

    public void setUserDegree(String userDegree) {
        this.userDegree = userDegree;
    }

    public String getIsMarried() {
        return isMarried;
    }

    public void setIsMarried(String isMarried) {
        this.isMarried = isMarried;
    }

    public String getHasChrild() {
        return hasChrild;
    }

    public void setHasChrild(String hasChrild) {
        this.hasChrild = hasChrild;
    }

    public String getFamilySum() {
        return familySum;
    }

    public void setFamilySum(String familySum) {
        this.familySum = familySum;
    }

    public String getLocationProvince() {
        return locationProvince;
    }

    public void setLocationProvince(String locationProvince) {
        this.locationProvince = locationProvince;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getCityType() {
        return cityType;
    }

    public void setCityType(String cityType) {
        this.cityType = cityType;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getHouseType() {
        return houseType;
    }

    public void setHouseType(String houseType) {
        this.houseType = houseType;
    }

    public String getHouseArea() {
        return houseArea;
    }

    public void setHouseArea(String houseArea) {
        this.houseArea = houseArea;
    }

    public String getFitmentType() {
        return fitmentType;
    }

    public void setFitmentType(String fitmentType) {
        this.fitmentType = fitmentType;
    }

    public String getEstateType() {
        return estateType;
    }

    public void setEstateType(String estateType) {
        this.estateType = estateType;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getTotalPrices() {
        return totalPrices;
    }

    public void setTotalPrices(String totalPrices) {
        this.totalPrices = totalPrices;
    }

    public String getDownPaymentPercent() {
        return downPaymentPercent;
    }

    public void setDownPaymentPercent(String downPaymentPercent) {
        this.downPaymentPercent = downPaymentPercent;
    }

    public String getDownPaymentPrices() {
        return downPaymentPrices;
    }

    public void setDownPaymentPrices(String downPaymentPrices) {
        this.downPaymentPrices = downPaymentPrices;
    }

    public String getLoansType() {
        return loansType;
    }

    public void setLoansType(String loansType) {
        this.loansType = loansType;
    }

    public String getHasCar() {
        return hasCar;
    }

    public void setHasCar(String hasCar) {
        this.hasCar = hasCar;
    }

    public String getHasCarport() {
        return hasCarport;
    }

    public void setHasCarport(String hasCarport) {
        this.hasCarport = hasCarport;
    }
}
