package com.tzp.LifeCycle.service;

import com.tzp.LifeCycle.dto.EsQueryDto;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ES文档类操作
 *
 * @author kangxvdong
 */
public interface EsDocumentService<T> {

    // 同步方法

    /**
     * 新增一个文档
     *
     * @param idxName 索引名
     * @param idxId 索引id
     * @param document 文档对象
     * @return 返回新增对象
     * @throws Exception 异常
     */
    IndexResponse createOneDocument(String idxName, String idxId, T document) throws Exception;

    /**
     * 批量增加文档
     *
     * @param idxName   索引名
     * @param documents 要增加的对象集合
     * @param idNameInJavaProject 在Java项目中，描述id（唯一标识符）的字段名
     * @return 批量操作的结果
     * @throws Exception 异常
     */
    BulkResponse batchCreateByCustomizationId(String idxName, List<Map<String, Object>> documents, String idNameInJavaProject) throws Exception;

    /**
     * 批量增加文档
     *
     * @param idxName   索引名
     * @param documents 要增加的对象集合
     * @return 批量操作的结果
     * @throws Exception 异常
     */
    BulkResponse batchCreate(String idxName, List<T> documents) throws Exception;

    /**
     * 用JSON字符串创建文档
     *
     * @param idxName 索引名
     * @param idxId 索引id
     * @param jsonContent json字符串
     * @return 返回创建对象
     * @throws Exception 异常
     */
    IndexResponse createByJson(String idxName, String idxId, String jsonContent) throws Exception;

    /**
     * 根据文档id删除文档
     *
     * @param idxName 索引名
     * @param docId   文档id
     * @return 删除结果
     */
    Boolean deleteById(String idxName, String docId);

    /**
     * 批量删除文档
     *
     * @param idxName 索引名
     * @param docIds 要删除的文档id集合
     * @return 返回批量处理对象
     * @throws Exception 异常
     */
    BulkResponse batchDeleteByIds(String idxName, List<String> docIds) throws Exception;

    /**
     * 根据文档id查找文档
     *
     * @param idxName 索引名
     * @param docId   文档id
     * @param clazz   转换对象
     * @return 查询结果
     * @throws IOException 异常
     */
    T getById(String idxName, String docId, Class<T> clazz) throws IOException;

    /**
     * 分页条件查询
     *
     * @param esQueryDTO 查询类
     * @return 查询结果
     * @throws IOException 异常
     */
    @Deprecated
    Map<String, Object> searchByQueryString(EsQueryDto<T> esQueryDTO) throws IOException;

    /**
     * 分页查询
     *
     * @return 返回数据
     * @param esQueryDTO 查询类
     * @throws IOException 异常
     */
    @Deprecated
    Map<String, Object> searchByPage(EsQueryDto<T> esQueryDTO) throws IOException;

    /**
     * 分页对象精确查询，字符模糊查询，某个字段某个值的
     *
     * @param esQueryDTO 查询类
     * @return 返回数据
     * @throws Exception 异常
     */
    Map<String, Object> searchByQueryObject(EsQueryDto<T> esQueryDTO) throws Exception;

    //    List<T> searchByQueryObjectMatchAndTerm(EsQueryDTO<T> esQueryDTO) throws Exception;

    /**
     * 修改索引文档的数据
     *
     * @param indexName 索引名
     * @param t 实体类对象
     * @param id 文档id
     * @return 返回是否修改成功，true成功，false失败
     * @throws Exception 异常
     */
    boolean updateById(String indexName, T t, String id) throws Exception;

    /**
     * 批量修改索引文档的数据
     *
     * @param indexName 索引名
     * @param list 数据对象列表
     * @param primaryKey 主键的名字
     * @return 返回批量修改是否成功，true成功，false（有部分）失败
     * @throws Exception 异常
     */
    boolean batchUpdateByIdMap(String indexName, List<Map<String, Object>> list, String primaryKey) throws Exception;

    /**
     * 批量修改索引文档的数据
     *
     * @param indexName 索引名
     * @param list 数据对象列表
     * @param clazz clazz
     * @param primaryKey 主键的名字
     * @return 返回批量修改是否成功，true成功，false（有部分）失败
     * @throws Exception 异常
     */
    boolean batchUpdateById(String indexName, List<T> list, Class<T> clazz, String primaryKey) throws Exception;

}