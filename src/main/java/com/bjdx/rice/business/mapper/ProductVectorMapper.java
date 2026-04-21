package com.bjdx.rice.business.mapper;

import com.bjdx.rice.business.entity.ProductVector;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品向量数据访问层
 * 
 * @author Rice System
 */
@Repository
public interface ProductVectorMapper {

    /**
     * 插入或更新商品向量
     */
    int upsert(ProductVector productVector);

    /**
     * 批量插入商品向量
     */
    int batchInsert(@Param("list") List<ProductVector> list);

    /**
     * 根据ID查询商品向量
     */
    ProductVector selectById(@Param("id") Long id);

    /**
     * 向量相似度搜索
     * @param vector 查询向量
     * @param topK 返回结果数
     * @return 相似的商品向量列表（包含相似度分数）
     */
    List<ProductVector> searchByVector(@Param("vector") float[] vector, @Param("topK") int topK);

    /**
     * 向量相似度搜索（带阈值过滤）
     * @param vector 查询向量
     * @param topK 返回结果数
     * @param threshold 相似度阈值
     * @return 相似的商品向量列表
     */
    List<ProductVector> searchByVectorWithThreshold(@Param("vector") float[] vector, 
                                                     @Param("topK") int topK, 
                                                     @Param("threshold") float threshold);

    /**
     * 删除商品向量
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除商品向量
     */
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * 清空所有商品向量
     */
    int deleteAll();

    /**
     * 查询所有商品向量（用于全量重建）
     */
    List<ProductVector> selectAll();

    /**
     * 统计商品向量数量
     */
    long count();
}
