#!/bin/bash

# ********************************************************************************
# 程序名称:
# 功能描述:    将云平台数据库mdc.base_user增量导入到hive中
# 输入参数:
#
# 输入资源:
# 输出资源:
#
# 中间资源:
# 创建人员:    Charles
# 创建日期:    2019-04-04
# 版本说明:    v1.0
# 修改人员:
# 修改日期:
# 修改原因:
# 版本说明:
# ********************************************************************************

if [ $# -eq 1 ];then
    updatetime=$1
    key=$2
else
    today=`date '+%Y-%m-%d'`
    updatetime=`date -d "${today} -1 days" '+%Y-%m-%d'`
fi

echo ${updatetime}

SQL="
CREATE TABLE IF NOT EXISTS test.base_user_java (
    uuid string COMMENT '人员编码',
    name string COMMENT '人员姓名',
    sex string COMMENT '人员性别',
    birth date COMMENT '出生日期',
    iden_type string COMMENT '证件类型',
    iden_num string COMMENT '证件号码',
    nation string COMMENT '民族',
    origin string COMMENT '籍贯',
    company string COMMENT '公司',
     phone string COMMENT '电话',
    email string COMMENT '邮件',
    description string COMMENT '描述',
    delete_flag int COMMENT '删除标志位:1-启用,0-删除',
    court_uuid string COMMENT '小区编码',
    create_time string COMMENT '创建时间',
    update_time string COMMENT '更新时间',
    create_user string COMMENT '创建人员',
    update_user string COMMENT '更新人员',
    finger_code1 string COMMENT '指纹1',
    finger_code2 string COMMENT '指纹2',
    finger_code3 string COMMENT '指纹3',
    face_pic string COMMENT '照片',
    password string COMMENT '密码',
    dept string COMMENT '部门',
    station string COMMENT '岗位',
    face_lib_id string COMMENT '图片库id',
    face_lib_pic string COMMENT '图片地址',
    focus_on_personnel string COMMENT '0.正常业主 1.长期不交物业管理费 2.曾经闹事人员 3.车辆冲闸逃费 4.嫌疑人物 5.其他',
    ac_flag int COMMENT '0-普通 1-业主是老人 2-业主是小孩'
) COMMENT '人员表'
ROW FORMAT delimited fields terminated by '\t'
STORED AS TEXTFILE;
"

HIVE2_SERVER="10.101.71.41:10000"
HADOOP_NAME="hive"
beeline -u jdbc:hive2://${HIVE2_SERVER} -n ${HADOOP_NAME} -e "${SQL}"

#数据库配置信息
PG_HOST="10.101.70.169"
PG_PORT="5432"
PG_SID="hdsc_db"
PG_CONNECT="jdbc:postgresql://$PG_HOST:$PG_PORT/$PG_SID"
PG_UNAME="hdsc_postgres"
PG_PWD="hdsc_postgres"
SCHEMA_NAME="mdc"



#sqoop导入数据
echo "sqoop import start..."

table_name="base_user"
target_dir="/user/hive/warehouse/test.db/base_user_java"
target_columns="uuid,name,sex,birth,iden_type,iden_num,nation,origin,company,phone,email,description,delete_flag,court_uuid,create_time,update_time,create_user,update_user,finger_code1,finger_code2,finger_code3,face_pic,
password,dept,station,face_lib_id,face_lib_pic,focus_on_personnel,ac_flag"

#condition="creat_time >= '${updatetime}' or update_time >= '${updatetime}' "

sqoop import \
--connect "$PG_CONNECT" \
--username "$PG_UNAME" \
--password "$PG_PWD" \
--table "$table_name" \
--columns "$target_columns" \
--target-dir "$target_dir" \
--fields-terminated-by '\t' \
--lines-terminated-by '\n' \
--hive-drop-import-delims \
--incremental lastmodified \
--merge-key uuid \
--check-column update_time \
--last-value "${updatetime}" \
--m 5 \
-- --schema "$SCHEMA_NAME" \
--null-string '\\N' \
--null-non-string '\\N'


echo "sqoop end..."


exitCode=$?
if [ $exitCode -ne 0 ];then
    echo "[ERROR] sqoop import execute failed!"
    exit $exitCode
fi