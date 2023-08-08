package com.tzp.LifeCycle.service;

/**
 * 创建es索引的具体方法
 *
 * @author kangxvdong
 */
public interface EsIndexService {

    /**
     * 新建索引，指定索引名称，指定创建json
     *
     * @param indexName 索引名
     * @param jsonString json字符串
     * @return boolean是否创建成功
     */
    boolean createIndex(String indexName, String jsonString);

    /**
     * 新建索引，指定索引名称，只需要提供索引名即可，使用默认的json
     *
     * @param indexName 索引名
     * @return boolean是否创建成功
     */
    boolean createIndex(String indexName);

    /**
     * 删除索引
     *
     * @param indexName 索引名
     * @return boolean 是否删除成功
     */
    boolean deleteIndex(String indexName);


    /**
     * 检查指定名称的索引是否存在
     *
     * @param indexName 索引名
     * @return - true：存在；false不存在
     */
    boolean indexExists(String indexName);

}
