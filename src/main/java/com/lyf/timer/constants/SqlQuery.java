package com.lyf.timer.constants;

/**
 * @Package : com.aisino.mysql.constants
 * @Class : SqlQuery
 * @Description : 拼装的SQL查询语句
 * @Author : liuyang
 * @CreateDate : 2017-08-18 星期五 22:17:14
 * @Version : V1.0.0
 * @Copyright : 2017 liuyang Inc. All rights reserved.
 */
public class SqlQuery {

    /*   #1   usercenter.`ucenter_user_service`表每天都有新增数据，需要将这些新增的数据与正式库和审核库数据建立关联关系*/
    /*   #1.1   查询 ucenter_user_service前一天新增的数据，即税号(c_taxnum)和服务单位ID(c_serviceid)*/
    public final static String SQL_QUERY_NEW_TB_US = "SELECT\n" +
            "  T_US.`c_taxnum` ,\n" +
            "  T_US.`c_serviceid` \n" +
            "  FROM\n" +
            "  usercenter.`ucenter_user_service` T_US\n" +
            "  WHERE\n" +
            "  T_US.`c_serviceid` IS NOT NULL\n" +
            "  AND T_US.`c_serviceid` != ''\n" +
            "  AND T_US.`c_taxnum` IS NOT NULL\n" +
            "  AND T_US.`c_taxnum` != ''\n" +
            "  AND T_US.`dt_adddate` >= DATE_SUB(NOW(), INTERVAL 1 DAY)\n" +
            "LIMIT ?, ?;";

    public final static String SQL_QUERY_NEW_TB_US_COUNT = "SELECT  count(T_US.`c_id`)\n" +
            "FROM\n" +
            "  usercenter.`ucenter_user_service` T_US\n" +
            "WHERE\n" +
            "  T_US.`c_serviceid` IS NOT NULL\n" +
            "  AND T_US.`c_serviceid` != ''\n" +
            "  AND T_US.`c_taxnum` IS NOT NULL\n" +
            "  AND T_US.`c_taxnum` != ''\n" +
            "  AND T_US.`dt_adddate` >= DATE_SUB(NOW(), INTERVAL 1 DAY);";


    /*   #1.2  更新关系表tb_code_serviceid*/
    /*  #1.2.1
         #创建维护表(dataserver.`tb_cmp_card` )中六位代码(code)和(usercenter.`ucenter_user_service`)中服务单位ID(serviceid)关系表*/
    public final static String SQL_CREATE_TB_RM = "CREATE TABLE\n" +
            "IF NOT EXISTS dataserver.`tb_code_serviceid` (\n" +
            "  `code`      VARCHAR(16),\n" +
            "  `serviceid` VARCHAR(30),\n" +
            "  KEY `code_index` (`code`) USING BTREE\n" +
            ");";

    /*   #1.2.2
          # 以税号为内连接的连接条件
          # 与表usercenter.`ucenter_user_service`建立关联关系*/
    public final static String SQL_INSERT_TB_RM = "   INSERT INTO dataserver.`tb_code_serviceid` (`code`, `serviceid`) (\n" +
            "      SELECT\n" +
            "        T_M.`code`,\n" +
            "        ?\n" +
            "      FROM\n" +
            "        dataserver.`tb_cmp_card` T_M\n" +
            "      WHERE\n" +
            "        T_M.`taxid` = ?\n" +
            "        AND T_M.`taxid` IS NOT NULL\n" +
            "    );";

    public final static String SQL_INSERT_TB_RA = "INSERT INTO dataserver.`tb_code_taxid_serviceid` (`code`, `taxid`, `serviceid`) (\n" +
            "SELECT DISTINCT\n" +
            "  T_A.`code`  AS `codeA`,\n" +
            "  T_A.`taxid` AS `taxidA`,\n" +
            "  ?\n" +
            "FROM\n" +
            "  dataserver.`tb_cmp_card_audit` T_A\n" +
            "  LEFT JOIN dataserver.`tb_cmp_card` T_M ON T_A.`code` = T_M.`code`\n" +
            "WHERE T_M.taxid = ?\n" +
            "      OR (\n" +
            "        T_A.taxid = ? AND T_A.code IS  NULL\n" +
            "      )\n" +
            "    );";

    /*   #2   dataserver中正式库和审核库表每天都有新增数据，需要将这些新增的数据与正式库和审核库数据建立关联关系*/
    /*查询正式库新增数据*/
    public final static String SQL_QUERY_NEW_TB_M = "SELECT \n" +
            "  T_M.`code` AS `code`,\n" +
            "  T_M.`taxid` AS `taxid`\n" +
            "FROM\n" +
            "  dataserver.`tb_cmp_card` T_M\n" +
            "WHERE\n" +
            "  T_M.`code` IS NOT NULL\n" +
            "  AND T_M.`taxid` IS NOT NULL\n" +
            "  AND T_M.`createtime` >= DATE_SUB(NOW(), INTERVAL 1 DAY)\n" +
            "LIMIT ?, ?;";
    /*查询正式库新增数据的数量*/
    public final static String SQL_QUERY_NEW_TB_M_COUNT = "SELECT COUNT(T_M.`id`)\n" +
            "FROM\n" +
            "  tb_cmp_card T_M\n" +
            "WHERE\n" +
            "  T_M.`code` IS NOT NULL\n" +
            "  AND T_M.`taxid` IS NOT NULL\n" +
            "  AND T_M.`createtime` >= DATE_SUB(NOW(), INTERVAL 1 DAY);";

    /*根据正式库查询出来的数据，查询与用户中心1天之前的数据的关联关系*/
    public final static String SQL_QUERY_OLD_TB_US_FOR_M = "SELECT\n" +
            "  ? AS 'code',\n" +
            "  T_US.`c_serviceid` AS `serviceid`\n" +
            "FROM\n" +
            "  usercenter.`ucenter_user_service` T_US\n" +
            "WHERE\n" +
            "  T_US.c_taxnum=? AND\n" +
            "   T_US.c_serviceid IS NOT NULL AND \n"+
            "  T_US.`dt_adddate` < DATE_SUB(NOW(), INTERVAL 1 DAY);";
    
    /*正式库新增数据的关联关系插入到关系表*/
    public final static String SQL_INSERT_NEW_TB_RM = "INSERT INTO dataserver.`tb_code_serviceid` (`code`,`serviceid`)VALUE (?, ?);";

    /*查询审核库新增数据*/
    public final static String SQL_QUERY_NEW_TB_A = "SELECT \n" +
            "  T_A.`code`  AS `code`,\n" +
            "  T_A.`taxid` AS `taxid`,\n" +
            "  T_M.`taxid` AS `taxidM`\n" +
            "FROM\n" +
            "  dataserver.`tb_cmp_card_audit` T_A\n" +
            "  LEFT JOIN dataserver.`tb_cmp_card` T_M ON T_A.`code` = T_M.`code`\n" +
            "WHERE\n" +
            "  T_A.`createtime` >= DATE_SUB(NOW(), INTERVAL 1 DAY)\n" +
            "LIMIT ?, ?;";
    /*查询审核库新增数据的数量*/
    public final static String SQL_QUERY_NEW_TB_A_COUNT = "SELECT COUNT(T_A.`id`)\n" +
            "FROM\n" +
            "  dataserver.`tb_cmp_card_audit` T_A\n" +
            "  LEFT JOIN dataserver.`tb_cmp_card` T_M ON T_A.`code` = T_M.`code`\n" +
            "WHERE\n" +
            "  T_A.`createtime` >= DATE_SUB(NOW(), INTERVAL 1 DAY);";
    
    /*根据审核库查询出来的数据，查询与用户中心1天之前的数据的关联关系*/
    public final static String SQL_QUERY_OLD_TB_US_FOR_A = "SELECT\n" +
            "  ? AS 'code',\n" +
            "  ? AS 'taxid',\n" +
            "  T_US.`c_serviceid` AS `serviceid`\n" +
            "FROM\n" +
            "  usercenter.`ucenter_user_service` T_US\n" +
            "WHERE\n" +
            "  T_US.c_taxnum=? AND\n" +
            "   T_US.c_serviceid IS NOT NULL AND \n"+
            "  T_US.`dt_adddate` < DATE_SUB(NOW(), INTERVAL 1 DAY);";

    /*审核库新增数据的关联关系插入到关系表*/
    public final static String SQL_INSERT_NEW_TB_RA = "INSERT INTO dataserver.`tb_code_taxid_serviceid` (`code`,`taxid`,`serviceid`)VALUE (?, ?, ?);";
}
