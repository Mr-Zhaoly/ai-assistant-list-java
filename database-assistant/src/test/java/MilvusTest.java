import com.zly.DatabaseAssistantApplication;
import com.zly.snowflake.component.IdGeneratorSnowflake;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = DatabaseAssistantApplication.class)
public class MilvusTest {

    @Resource(name = "mysqlVectorStore")
    private VectorStore mysqlVectorStore;

    @Autowired
    private IdGeneratorSnowflake idGeneratorSnowflake;
    @Test
    public void testMilnvs() {
        List<Document> documents = List.of(
                Document.builder()
                        .id(idGeneratorSnowflake.nextId())
                        .text("CREATE TABLE `gerpgo_shop` (\n" +
                                "  `id` bigint(20) NOT NULL COMMENT 'ID',\n" +
                                "  `shop_id` varchar(50) DEFAULT NULL COMMENT '店铺id（亚马逊取值marketListVos--sellerId+marketId）',\n" +
                                "  `jijia_shop_id` varchar(50) DEFAULT NULL COMMENT '系统店铺id（shopId/sellerId+ \"_\" + site_name）',\n" +
                                "  `market_id` int(4) DEFAULT NULL COMMENT '站点ID',\n" +
                                "  `shop_name` varchar(100) DEFAULT NULL COMMENT '店铺名称',\n" +
                                "  `site_name` varchar(50) DEFAULT NULL COMMENT '站点名称',\n" +
                                "  `platform` varchar(50) DEFAULT NULL COMMENT '平台',\n" +
                                "  `brand_code` varchar(5) DEFAULT NULL COMMENT '品牌code',\n" +
                                "  `country_code` varchar(50) DEFAULT NULL,\n" +
                                "  `is_auto_transfer_stock` tinyint(1) DEFAULT '0' COMMENT '是否开启自动借调库存（0 否 1 是）',\n" +
                                "  `custom_platform` varchar(50) DEFAULT NULL COMMENT '销售平台',\n" +
                                "  `custom_depart_id` varchar(50) DEFAULT NULL COMMENT '销售部门id',\n" +
                                "  `custom_site_name` varchar(50) DEFAULT NULL COMMENT '站点地区',\n" +
                                "  `proportion` int(2) DEFAULT '1' COMMENT '分配比例',\n" +
                                "  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
                                "  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人id',\n" +
                                "  `created_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',\n" +
                                "  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                                "  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人id',\n" +
                                "  `updated_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',\n" +
                                "  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0 否 1 是）',\n" +
                                "  PRIMARY KEY (`id`),\n" +
                                "  KEY `idx_shop_id` (`shop_id`,`is_delete`)\n" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积加店铺信息';")
                        .metadata(Map.of("table", "gerpgo_shop"))
                        .build(),
                Document.builder()
                        .id(idGeneratorSnowflake.nextId())
                        .text("CREATE TABLE `gerpgo_order` (\n" +
                                "  `id` bigint(20) NOT NULL COMMENT 'ID',\n" +
                                "  `order_no` varchar(50) NOT NULL COMMENT '平台订单号',\n" +
                                "  `gerpgo_uni_id` varchar(255) NOT NULL COMMENT '积加订单唯一标识',\n" +
                                "  `ec_order_id` int(4) DEFAULT NULL COMMENT '易仓订单主键ID',\n" +
                                "  `erp_order_no` varchar(100) DEFAULT NULL COMMENT 'erp订单号',\n" +
                                "  `platform` varchar(50) DEFAULT NULL COMMENT '平台编码',\n" +
                                "  `order_status` tinyint(4) DEFAULT '1' COMMENT '订单状态: 1待付款、2待发货、3已发货、4部分发货、5已完成、6已取消',\n" +
                                "  `third_warehouse_id` bigint(20) DEFAULT NULL COMMENT 'gerpgo_third_warehouse三方仓id',\n" +
                                "  `shop_id` bigint(20) DEFAULT NULL COMMENT 'gerpgo_shop店铺id',\n" +
                                "  `shop_name` varchar(255) DEFAULT NULL COMMENT '店铺名称',\n" +
                                "  `shop_country` varchar(255) DEFAULT NULL COMMENT '店铺站点',\n" +
                                "  `delivery_way` tinyint(4) DEFAULT NULL COMMENT '配送方式：1自发货、2FBA、3平台仓发货',\n" +
                                "  `order_amount` decimal(15,4) DEFAULT '0.0000' COMMENT '订单金额',\n" +
                                "  `pay_amount` decimal(15,4) DEFAULT '0.0000' COMMENT '支付金额',\n" +
                                "  `tax_amount` decimal(15,4) DEFAULT '0.0000' COMMENT '税费金额',\n" +
                                "  `shipping_amount` decimal(15,4) DEFAULT '0.0000' COMMENT '运费金额',\n" +
                                "  `discount_amount` decimal(15,4) DEFAULT '0.0000' COMMENT '折扣金额',\n" +
                                "  `other_fee` decimal(15,4) DEFAULT '0.0000' COMMENT '其他费用',\n" +
                                "  `currency` varchar(10) DEFAULT NULL COMMENT '币种',\n" +
                                "  `buyer_id` varchar(50) DEFAULT NULL COMMENT '买家ID',\n" +
                                "  `buyer_email` varchar(255) DEFAULT NULL COMMENT '买家邮箱',\n" +
                                "  `buyer_name` varchar(255) DEFAULT NULL COMMENT '买家姓名',\n" +
                                "  `buyer_remark` text COMMENT '买家备注',\n" +
                                "  `consignee_name` varchar(255) DEFAULT NULL COMMENT '收件人姓名',\n" +
                                "  `consignee_email` varchar(255) DEFAULT NULL COMMENT '收件人邮箱',\n" +
                                "  `consignee_phone` varchar(255) DEFAULT NULL COMMENT '收件人电话',\n" +
                                "  `consignee_postal_code` varchar(50) DEFAULT NULL COMMENT '收件人邮编',\n" +
                                "  `consignee_country` varchar(255) DEFAULT NULL COMMENT '收件人国家',\n" +
                                "  `consignee_province` varchar(255) DEFAULT NULL COMMENT '收件人省份',\n" +
                                "  `consignee_city` varchar(255) DEFAULT NULL COMMENT '收件人城市',\n" +
                                "  `consignee_area` varchar(255) DEFAULT NULL COMMENT '收件人区',\n" +
                                "  `consignee_street` varchar(255) DEFAULT NULL COMMENT '收件人街道',\n" +
                                "  `consignee_house_number` varchar(255) DEFAULT NULL COMMENT '收件人门牌号',\n" +
                                "  `consignee_address` varchar(1024) DEFAULT NULL COMMENT '收件人详细地址',\n" +
                                "  `customer_remark` text COMMENT '客服备注',\n" +
                                "  `order_create_time` datetime DEFAULT NULL COMMENT '订单创建时间',\n" +
                                "  `order_update_time` datetime DEFAULT NULL COMMENT '订单更新时间',\n" +
                                "  `order_time` datetime DEFAULT NULL COMMENT '订购时间',\n" +
                                "  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',\n" +
                                "  `shipping_time` datetime DEFAULT NULL COMMENT '发货时间',\n" +
                                "  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',\n" +
                                "  `remark` varchar(255) DEFAULT NULL COMMENT '备注',\n" +
                                "  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
                                "  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人id',\n" +
                                "  `created_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',\n" +
                                "  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                                "  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人id',\n" +
                                "  `updated_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',\n" +
                                "  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0 否 1 是）',\n" +
                                "  PRIMARY KEY (`id`) USING BTREE,\n" +
                                "  UNIQUE KEY `uni_platform_uni_id` (`platform`,`gerpgo_uni_id`),\n" +
                                "  KEY `index_order_no` (`order_no`) USING BTREE,\n" +
                                "  KEY `idx_erp_order_no` (`erp_order_no`),\n" +
                                "  KEY `idx_ec_order_id` (`ec_order_id`),\n" +
                                "  KEY `idx_email_phone` (`buyer_email`,`consignee_phone`)\n" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积加订单表';")
                        .metadata(Map.of("table", "gerpgo_order"))
                        .build(),
                Document.builder()
                        .id(idGeneratorSnowflake.nextId())
                        .text("CREATE TABLE `gerpgo_order_product` (\n" +
                                "  `id` bigint(20) NOT NULL COMMENT 'ID',\n" +
                                "  `gerpgo_order_id` bigint(20) DEFAULT NULL COMMENT '积加订单id',\n" +
                                "  `order_package_id` bigint(20) DEFAULT NULL COMMENT '积加拆单id',\n" +
                                "  `ec_order_detail_id` int(4) DEFAULT NULL COMMENT '易仓详情主键ID',\n" +
                                "  `type` tinyint(1) DEFAULT '1' COMMENT '产品所属类型：1订单、2拆单',\n" +
                                "  `product_title` varchar(1000) DEFAULT NULL COMMENT '产品名称',\n" +
                                "  `sku` varchar(255) DEFAULT NULL COMMENT 'sku',\n" +
                                "  `msku` varchar(255) DEFAULT NULL COMMENT 'msku',\n" +
                                "  `third_sku` varchar(255) DEFAULT NULL COMMENT '三方仓sku',\n" +
                                "  `unit_price` decimal(15,4) DEFAULT '0.0000' COMMENT '单价',\n" +
                                "  `discount_amount` decimal(15,4) DEFAULT '0.0000' COMMENT '折扣金额',\n" +
                                "  `quantity` int(4) DEFAULT '0' COMMENT '数量',\n" +
                                "  `currency` varchar(10) DEFAULT NULL COMMENT '币种',\n" +
                                "  `remark` varchar(255) DEFAULT NULL COMMENT '备注',\n" +
                                "  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
                                "  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人id',\n" +
                                "  `created_user_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',\n" +
                                "  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
                                "  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人id',\n" +
                                "  `updated_user_name` varchar(50) DEFAULT NULL COMMENT '更新人名称',\n" +
                                "  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0 否 1 是）',\n" +
                                "  PRIMARY KEY (`id`) USING BTREE,\n" +
                                "  KEY `index_gerpgo_order_id` (`gerpgo_order_id`),\n" +
                                "  KEY `idx_gerpgo_order_id_type_sku` (`gerpgo_order_id`,`type`,`sku`,`is_delete`),\n" +
                                "  KEY `idx_ec_order_detail_id` (`ec_order_detail_id`,`is_delete`)\n" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积加订单商品表';")
                        .metadata(Map.of("table", "gerpgo_order_product"))
                        .build()
                );

        // Add the documents to Milvus Vector Store
        mysqlVectorStore.add(documents);

        // Retrieve documents similar to a query
//        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
//        System.out.println(results);
    }

}
