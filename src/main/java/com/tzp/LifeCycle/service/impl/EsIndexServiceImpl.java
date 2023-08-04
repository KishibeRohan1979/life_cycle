package com.tzp.LifeCycle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tzp.LifeCycle.service.EsIndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 创建es索引的具体方法
 *
 * @author kangxvdong
 */

@Slf4j
@Service
public class EsIndexServiceImpl implements EsIndexService {

    private final String INDEX_SET_MAP = "{\n" +
            "  \"settings\": {\n" +
            "    \"analysis\": {\n" +
            "      \"analyzer\": {\n" +
            "        \"ik_max_word\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"tokenizer\": \"ik_max_word\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"dynamic_templates\": [\n" +
            "      {\n" +
            "        \"strings\": {\n" +
            "          \"match_mapping_type\": \"string\",\n" +
            "          \"mapping\": {\n" +
            "            \"type\": \"text\",\n" +
            "            \"analyzer\": \"ik_max_word\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 新建索引，指定索引名称，指定创建json
     *
     * @param indexName  索引名
     * @param jsonString json字符串
     * @return boolean是否创建成功
     * @throws Exception 异常
     */
    @Override
    public boolean createIndex(String indexName, String jsonString) throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        JSONObject jsonObject  = JSON.parseObject(jsonString);
        if ( jsonObject != null && !"".equals(jsonObject.toString()) ) {
            request.source(jsonObject.toString(), XContentType.JSON);
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        }
        return false;
    }

    /**
     * 新建索引，指定索引名称，只需要提供索引名即可，使用默认的json
     *
     * @param indexName 索引名
     * @return boolean是否创建成功
     * @throws Exception 异常
     */
    @Override
    public boolean createIndex(String indexName) throws Exception {
        return createIndex(indexName, INDEX_SET_MAP);
    }

    /**
     * 删除索引
     *
     * @param indexName 索引名
     * @return boolean 是否删除成功
     * @throws Exception 异常
     */
    @Override
    public boolean deleteIndex(String indexName) throws Exception {
        AcknowledgedResponse response = restHighLevelClient.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    /**
     * 检查指定名称的索引是否存在
     *
     * @param indexName 索引名
     * @return - true：存在；false不存在
     * @throws Exception 异常
     */
    @Override
    public boolean indexExists(String indexName) throws Exception {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }
}
