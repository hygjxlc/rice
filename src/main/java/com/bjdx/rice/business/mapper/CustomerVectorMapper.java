package com.bjdx.rice.business.mapper;

import com.bjdx.rice.business.entity.CustomerVector;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户向量数据访问层
 * 
 * @author Rice System
 */
@Repository
public interface CustomerVectorMapper {

    /**
     * 插入或更新客户向量
     */
    int upsert(CustomerVector customerVector);

    /**
     * 批量插入客户向量
     */
    int batchInsert(@Param("list") List<CustomerVector> list);

    /**
     * 根据ID查询客户向量
     */
    CustomerVector selectById(@Param("id") Long id);

    /**
     * 向量相似度搜索
     * @param vector 查询向量
     * @param topK 返回结果数
     * @return 相似的客户向量列表（包含相似度分数）
     */
    List<CustomerVector> searchByVector(@Param("vector") float[] vector, @Param("topK") int topK);

    /**
     * 向量相似度搜索（带阈值过滤）
     * @param vector 查询向量
     * @param topK 返回结果数
     * @param threshold 相似度阈值
     * @return 相似的客户向量列表
     */
    List<CustomerVector> searchByVectorWithThreshold(@Param("vector") float[] vector, 
                                                      @Param("topK") int topK, 
                                                      @Param("threshold") float threshold);

    /**
     * 删除客户向量
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除客户向量
     */
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * 清空所有客户向量
     */
    int deleteAll();

    /**
     * 查询所有客户向量（用于全量重建）
     */
    List<CustomerVector> selectAll();

    /**
     * 统计客户向量数量
     */
    long count();
}
