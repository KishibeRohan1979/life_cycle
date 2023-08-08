package com.tzp.LifeCycle.service.impl;

import com.alibaba.fastjson.JSON;
import com.tzp.LifeCycle.dto.EsQueryDto;
import com.tzp.LifeCycle.service.EsDocumentService;
import com.tzp.LifeCycle.util.LifeStringUtil;
import com.tzp.LifeCycle.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kangxvdong
 */
@Slf4j
@Service
public class EsDocumentServiceImpl<T> implements EsDocumentService<T> {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 新增一个文档
     *
     * @param idxName  索引名
     * @param idxId    索引id
     * @param document 文档对象
     * @throws Exception 异常
     */
    @Override
    public IndexResponse createOneDocument(String idxName, String idxId, T document) throws Exception {
        IndexRequest request = new IndexRequest();
        request.index(idxName);
        request.id(idxId);
        request.source(JSON.toJSONString(document, false), XContentType.JSON);
        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量增加文档
     *
     * @param idxName   索引名
     * @param documents 要增加的对象集合
     * @return 批量操作的结果
     * @throws Exception 异常
     */
    @Override
    public BulkResponse batchCreate(String idxName, List<T> documents) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        // 批量请求处理
        for (T document : documents) {
            bulkRequest.add(
                    // 这里是数据信息
                    new IndexRequest(idxName)
                            .source(JSON.toJSONString(document, false), XContentType.JSON)
            );
        }
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 用JSON字符串创建文档
     *
     * @param idxName     索引名
     * @param idxId       索引id
     * @param jsonContent json字符串
     * @return 返回创建对象
     * @throws Exception 异常
     */
    @Override
    public IndexResponse createByJson(String idxName, String idxId, String jsonContent) throws Exception {
        IndexRequest request = new IndexRequest();
        request.index(idxName);
        request.id(idxId);
        // JSON.toJSONString可能多此一举，后期再优化
        request.source(JSON.toJSONString(jsonContent), XContentType.JSON);
        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 根据文档id删除文档
     *
     * @param idxName 索引名
     * @param docId   文档id
     * @return 删除结果
     */
    @Override
    public Boolean deleteById(String idxName, String docId) {
        DeleteRequest request = new DeleteRequest();
        request.index(idxName);
        request.id(docId);
        try {
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量删除文档
     *
     * @param idxName 索引名
     * @param docIds  要删除的文档id集合
     * @return 返回批量处理对象
     * @throws Exception 异常
     */
    @Override
    public BulkResponse batchDeleteByIds(String idxName, List<String> docIds) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        // 批量请求处理
        for (String docId : docIds) {
            bulkRequest.add(
                    // 这里是数据信息
                    new DeleteRequest()
                            .index(idxName)
                            .id(docId)
            );
        }
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 根据文档id查找文档
     *
     * @param idxName 索引名
     * @param docId   文档id
     * @param clazz   转换对象
     * @return 查询结果
     * @throws IOException 异常
     */
    @Override
    public T getById(String idxName, String docId, Class<T> clazz) throws IOException {
        GetRequest request = new GetRequest();
        request.index(idxName);
        request.id(docId);
        GetResponse getResponse = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        // 判断文档是否存在
        if (getResponse.isExists()) {
            String sourceAsString = getResponse.getSourceAsString();
            return JSON.parseObject(sourceAsString, clazz);
        } else {
            return null;
        }
    }

    /**
     * 分页条件查询
     *
     * @param esQueryDTO 查询类
     * @return 查询结果
     * @throws IOException 异常
     */
    @Override
    public Map<String, Object> searchByQueryString(EsQueryDto<T> esQueryDTO) throws IOException {
        Long pageNo = esQueryDTO.getPageNum();
        Long pageSize = esQueryDTO.getPageSize();
        // 1.创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(esQueryDTO.getIndexName());
        // 2.构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // (1)查询条件 使用QueryBuilders工具类创建
        // 多字段模糊查询
        // 查询所有字符串类型的字段
        String[] fields = {"*"};
        String analyzerType = PageUtil.getAnalyzerType(esQueryDTO.getAnalyzerType());
        String queryString = PageUtil.deleteNull(esQueryDTO.getQueryString());
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(queryString, fields).analyzer(analyzerType);
        searchSourceBuilder.from(pageNo.intValue() * pageSize.intValue());
        searchSourceBuilder.size(pageSize.intValue());
        // (2) 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 高亮所有字段
        highlightBuilder.field("*");
        // 多个字段时，匹配不同的字段时需要匹配高亮
        highlightBuilder.requireFieldMatch(false);
        // 高亮前缀
        highlightBuilder.preTags("<font color='red'>");
        // 高亮后缀
        highlightBuilder.postTags("</font>");
        // 不进行限制，完整显示所有高亮内容
        highlightBuilder.numOfFragments(0);
        searchSourceBuilder.highlighter(highlightBuilder);
        // (3)条件投入
        searchSourceBuilder.query(queryBuilder);
        // 3.添加条件到请求
        searchRequest.source(searchSourceBuilder);
        // 4.客户端查询请求
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 5.查看返回结果
        List<T> resultList = getResultList(search, esQueryDTO.getQueryClazz());
        return getResultMap(resultList, search.getHits().getTotalHits().value, pageSize, pageNo);
    }

    /**
     * 分页查询
     *
     * @param esQueryDTO 查询类
     * @return 返回数据
     * @throws IOException 异常
     */
    @Override
    public Map<String, Object> searchByPage(EsQueryDto<T> esQueryDTO) throws IOException {
        Long pageNo = esQueryDTO.getPageNum();
        Long pageSize = esQueryDTO.getPageSize();
        List<T> resultList = new ArrayList<>();
        // 1.创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(esQueryDTO.getIndexName());

        // 2.构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(pageNo.intValue() * pageSize.intValue());
        searchSourceBuilder.size(pageSize.intValue());

        // 3.添加条件到请求
        searchRequest.source(searchSourceBuilder);

        // 4.客户端查询请求
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 5.查看返回结果
        SearchHits hits = search.getHits();
        SearchHit[] hitsArray = hits.getHits();
        for (SearchHit hit : hitsArray) {
            Map<String, Object> map = hit.getSourceAsMap();
            resultList.add(JSON.parseObject(JSON.toJSONString(map), esQueryDTO.getQueryClazz()));
        }
        return getResultMap(resultList, search.getHits().getTotalHits().value, pageSize, pageNo);
    }

    /**
     * 分页对象精确查询，字符模糊查询，某个字段某个值的
     *
     * @param esQueryDTO 查询类
     * @return 返回数据
     * @throws Exception 异常
     */
    @Override
    public Map<String, Object> searchByQueryObject(EsQueryDto<T> esQueryDTO) throws Exception {
        Long pageNo = esQueryDTO.getPageNum();
        Long pageSize = esQueryDTO.getPageSize();
        // 1.创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(esQueryDTO.getIndexName());
        // 2.构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // (1)查询条件 使用QueryBuilders工具类创建
        // 多字段模糊查询
        // 查询所有字符串类型的字段
        String[] fields = {"*"};
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 多字段模糊查询
        String analyzerType = PageUtil.getAnalyzerType(esQueryDTO.getAnalyzerType());
        if (!StringUtils.isBlank(esQueryDTO.getQueryString())) {
            String queryString = PageUtil.deleteNull(esQueryDTO.getQueryString());
            // 设置查询条件
            MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(queryString, fields)
                    .analyzer(analyzerType);
            searchSourceBuilder.query(boolQueryBuilder.must(queryBuilder));
        }
        // 精确查询
        if (esQueryDTO.getQueryObject() != null) {
            // (2) 精确匹配和范围查询过滤条件
            Map<String, Object> javaMap = new HashMap<>();
            // 利用Java反射获取泛型t的属性
            Class<?> tClass = esQueryDTO.getQueryObject().getClass();
            Field[] javaFields = tClass.getDeclaredFields();
            for (Field field : javaFields) {
                String attributeName = field.getName();
//                Class<?> attributeType = field.getType()
                // 转化type为自定义对象
//            Object = type.getDeclaredConstructor().newInstance()
                // 获取get...（get属性）方法
                Method getNameMethod = tClass.getMethod("get"+ LifeStringUtil.capitalizeFirstLetter(attributeName));
                // 调用get...（get属性）方法
                Object nameValue = getNameMethod.invoke(esQueryDTO.getQueryObject());
                if ( null != nameValue ) {
                    javaMap.put(attributeName, nameValue);
                }
            }
            // 精确匹配字段
            for (Map.Entry<String, Object> entry : javaMap.entrySet()) {
                TermQueryBuilder termQuery = QueryBuilders.termQuery(entry.getKey(), entry.getValue());
                boolQueryBuilder.filter(termQuery);
            }
        }
        // 排序查询
        if (!StringUtils.isBlank(esQueryDTO.getOrderField())) {
            SortBuilder<FieldSortBuilder> sortBuilder = SortBuilders.fieldSort(esQueryDTO.getOrderField());
            if ( "asc".equals(esQueryDTO.getOrderType()) ) {
                sortBuilder.order(SortOrder.ASC);
            } else {
                sortBuilder.order(SortOrder.DESC);
            }
            searchSourceBuilder.sort(sortBuilder);
        }
        // 范围查询
        if (!StringUtils.isBlank(esQueryDTO.getRangeField())) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(esQueryDTO.getRangeField()).gte(esQueryDTO.getStartValue()).lt(esQueryDTO.getEndValue());
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        // 模糊查询字段
        if (esQueryDTO.getMatchMap() != null) {
            for (Map.Entry<String, Object> entry : esQueryDTO.getMatchMap().entrySet()) {
                MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(entry.getValue().toString(), entry.getKey())
                        .analyzer(analyzerType);
                boolQueryBuilder.filter(queryBuilder);
            }
        }
        // 精确查询字段
        if (esQueryDTO.getTermMap() != null) {
            for (Map.Entry<String, Object> entry : esQueryDTO.getTermMap().entrySet()) {
                TermQueryBuilder termQuery = QueryBuilders.termQuery(entry.getKey(), entry.getValue());
                boolQueryBuilder.filter(termQuery);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        // (3) 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 高亮所有字段
        highlightBuilder.field("*");
        // 多个字段时，匹配不同的字段时需要匹配高亮
        highlightBuilder.requireFieldMatch(false);
        // 高亮前缀
        highlightBuilder.preTags("<font color='red'>");
        // 高亮后缀
        highlightBuilder.postTags("</font>");
        // 不进行限制，完整显示所有高亮内容
        highlightBuilder.numOfFragments(0);
        searchSourceBuilder.highlighter(highlightBuilder);
        // 4.添加条件到请求
        searchSourceBuilder.from(pageNo.intValue() * pageSize.intValue());
        searchSourceBuilder.size(pageSize.intValue());
        searchRequest.source(searchSourceBuilder);
        // 5.客户端
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 5.查看返回结果
        List<T> resultList = getResultList(search, esQueryDTO.getQueryClazz());
        return getResultMap(resultList, search.getHits().getTotalHits().value, pageSize, pageNo);
    }

    /**
     * 修改索引的数据
     *
     * @param indexName 索引名
     * @param t         实体类对象
     * @param id        文档id
     * @param clazz     clazz  封装的实现
     * @return 返回是否修改成功
     */
    @Override
    public boolean updateById(String indexName, T t, String id, Class<T> clazz) throws Exception {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(indexName);
        updateRequest.id(id);
        updateRequest.doc(getBuilder(t, clazz));

        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

        return updateResponse.getResult() == DocWriteResponse.Result.UPDATED;
    }

    /**
     * 批量修改索引文档的数据
     *
     * @param indexName 索引名
     * @param list      数据对象列表
     * @param clazz     clazz
     * @return 返回批量修改是否成功
     */
    @Override
    public boolean batchUpdateById(String indexName, List<T> list, Class<T> clazz) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        for (T object : list) {
            // 这个方法有些冒险，需要我们提前约定好，ES的实体类一定要包括id，所以ES的对象需要统一继承 某个 这个父类
            String docId = getIdByEsObject(object, clazz);

            UpdateRequest updateRequest = new UpdateRequest(indexName, docId)
                    .doc(getBuilder(object, clazz));

            bulkRequest.add(updateRequest);
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        return !bulkResponse.hasFailures();
    }

    /**
     * 返回结果，将结果用前缀和后缀包裹好
     *
     * @param search search对象
     * @param clazz 转换对象
     * @return 返回结果列表
     */
    private List<T> getResultList(SearchResponse search, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        // 5.查看返回结果
        SearchHits hits = search.getHits();
        SearchHit[] hitsArray = hits.getHits();
        // 文档的循环
        for (SearchHit hit : hitsArray) {
            Map<String, Object> map = hit.getSourceAsMap();
            // 获取高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            // 高亮字段的循环
            for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
                String fieldName = entry.getKey();
                HighlightField highlight = entry.getValue();
                Text[] fragments = highlight.fragments();
                StringBuilder fragmentString = new StringBuilder();
                // 字段（例：id、name什么）的循环
                for (Text fragment : fragments) {
                    fragmentString.append(fragment);
                }
                // 替换原有字段值
                map.put(fieldName, fragmentString.toString());
            }
            resultList.add(JSON.parseObject(JSON.toJSONString(map), clazz));
        }
        return resultList;
    }

    /**
     * 对结果进行封装
     *
     * @param resultList 结果列表
     * @param total 总行数
     * @param pageSize 当前页返回多少行
     * @param pageNo 当前第几页
     * @return 返回对象
     */
    private Map<String, Object> getResultMap(List<T> resultList, Long total, Long pageSize, Long pageNo) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("page", PageUtil.getPage(total, pageSize, pageNo));
        resultMap.put("data", resultList);
        return resultMap;
    }

    /**
     * 使用反射来构建要更新的内容，将对象中的属性以字段名和值的形式添加到 XContentBuilder 中。
     *
     * @param t 要更新的对象
     * @param clazz 转换使用的类
     * @return xContentBuilder
     * @throws Exception 各种转化异常
     */
    private XContentBuilder getBuilder(T t, Class<T> clazz) throws Exception {
        // 下面两行，固定写法，无需在意
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();
        // 通过 BeanUtils 获取给定对象的属性描述符。属性描述符包含了对象的属性名、读取方法和写入方法等信息
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);

        // 遍历所有属性描述符
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            // 获取当前属性描述符的属性名
            String propertyName = propertyDescriptor.getName();
            // 这个"class"是 Java 对象的默认属性，需要过滤掉
            if (!"class".equals(propertyName)) {
                // 通过属性描述符的读取方法获取属性值
                Object propertyValue = propertyDescriptor.getReadMethod().invoke(t);
                // 将属性名和属性值添加到 XContentBuilder 对象中
                xContentBuilder.field(propertyName, propertyValue);
            }
        }

        xContentBuilder.endObject();
        return xContentBuilder;
    }

    /**
     * 利用反射将对象中的id值返回回去
     *
     * @param t 对象
     * @param clazz 对象类型
     * @return id值
     * @throws Exception 异常
     */
    private String getIdByEsObject(T t, Class<T> clazz) throws Exception {
        // 通过 BeanUtils 获取给定对象的属性描述符。属性描述符包含了对象的属性名、读取方法和写入方法等信息
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);

        // 遍历所有属性描述符
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            // 获取当前属性描述符的属性名
            String propertyName = propertyDescriptor.getName();
            if ("id".equals(propertyName)) {
                // 通过属性描述符的读取方法获取属性值
                Object propertyValue = propertyDescriptor.getReadMethod().invoke(t);
                return propertyValue.toString();
            }
        }

        return "";
    }

}
