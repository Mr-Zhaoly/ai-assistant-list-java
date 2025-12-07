package com.zly.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreAutoConfiguration {

    @Value("${milvus.client.url}")
    private String url;

    @Value("${milvus.client.userName}")
    private String userName;

    @Value("${milvus.client.password}")
    private String password;

    @Bean(name = "mysqlVectorStore")
    public VectorStore mysqlVectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName("table_mysql_collection")
                .databaseName("database_assistant")
                .embeddingDimension(1024)
                .indexType(IndexType.FLAT)
                .metricType(MetricType.COSINE)
                .autoId( true)
                .iDFieldName("id")
                .embeddingFieldName("vector")
                .metadataFieldName("meta")
                .contentFieldName("content")
                .batchingStrategy(new TokenCountBatchingStrategy())
                .initializeSchema(true)
                .build();
    }

    @Bean
    public MilvusServiceClient milvusClient() {
        return new MilvusServiceClient(ConnectParam.newBuilder()
                .withAuthorization(userName, password)
                .withUri(url)
                .build());
    }
}
