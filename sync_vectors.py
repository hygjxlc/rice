#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
向量数据同步脚本
从 MySQL 读取客户和商品数据，生成 N-Gram 向量，同步到 PostgreSQL 向量数据库
"""

import pymysql
import psycopg2
import numpy as np
import hashlib
import re
from collections import Counter

# 数据库配置
MYSQL_CONFIG = {
    'host': '192.168.16.41',
    'port': 3306,
    'user': 'root',
    'password': '123456Aa',
    'database': 'rice',
    'charset': 'utf8mb4'
}

PG_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'user': 'rice',
    'password': 'Rice@2024!',
    'database': 'rice_vector'
}

# N-Gram 配置
MIN_N = 2
MAX_N = 4
VECTOR_DIMENSION = 1024

def normalize_text(text):
    """文本预处理"""
    if not text:
        return ""
    # 转换为小写，去除多余空格
    text = str(text).lower().strip()
    # 保留中文、英文、数字
    text = re.sub(r'[^\u4e00-\u9fa5a-z0-9]', '', text)
    return text

def extract_ngram_features(text):
    """提取 N-Gram 特征"""
    features = []
    text = normalize_text(text)
    if not text:
        return features
    
    for n in range(MIN_N, min(MAX_N + 1, len(text) + 1)):
        for i in range(len(text) - n + 1):
            ngram = text[i:i+n]
            features.append(ngram)
    
    return features

def build_vector(text):
    """构建 N-Gram 向量"""
    ngrams = extract_ngram_features(text)
    if not ngrams:
        return [0.0] * VECTOR_DIMENSION
    
    # 统计 N-Gram 频率
    ngram_counts = Counter(ngrams)
    
    # 构建向量（使用哈希位置）
    vector = [0.0] * VECTOR_DIMENSION
    for ngram, count in ngram_counts.items():
        # 使用哈希确定位置
        hash_val = int(hashlib.md5(ngram.encode('utf-8')).hexdigest(), 16)
        idx = hash_val % VECTOR_DIMENSION
        vector[idx] += count
    
    # L2 归一化
    norm = np.linalg.norm(vector)
    if norm > 0:
        vector = [v / norm for v in vector]
    
    return vector

def sync_customers():
    """同步客户数据"""
    print("开始同步客户数据...")
    
    # 连接 MySQL
    mysql_conn = pymysql.connect(**MYSQL_CONFIG)
    mysql_cursor = mysql_conn.cursor()
    
    # 连接 PostgreSQL
    pg_conn = psycopg2.connect(**PG_CONFIG)
    pg_cursor = pg_conn.cursor()
    
    try:
        # 清空现有数据
        pg_cursor.execute("TRUNCATE TABLE customer_vectors")
        pg_conn.commit()
        print("已清空 customer_vectors 表")
        
        # 读取客户数据
        mysql_cursor.execute("""
            SELECT id, customer_name, customer_code 
            FROM customer_info 
            WHERE is_deleted = 0 AND customer_name IS NOT NULL
        """)
        
        customers = mysql_cursor.fetchall()
        print(f"从 MySQL 读取到 {len(customers)} 条客户数据")
        
        # 插入向量数据
        inserted = 0
        for customer in customers:
            customer_id, customer_name, customer_code = customer
            
            # 生成向量
            vector = build_vector(customer_name)
            vector_str = '[' + ','.join([f'{v:.6f}' for v in vector]) + ']'
            
            # 插入 PostgreSQL
            pg_cursor.execute("""
                INSERT INTO customer_vectors (customer_id, customer_name, customer_code, vector)
                VALUES (%s, %s, %s, %s::vector(1024))
            """, (customer_id, customer_name, customer_code or '', vector_str))
            
            inserted += 1
            if inserted % 100 == 0:
                pg_conn.commit()
                print(f"已同步 {inserted} 条客户数据...")
        
        pg_conn.commit()
        print(f"客户数据同步完成，共 {inserted} 条")
        
    except Exception as e:
        print(f"同步客户数据出错: {e}")
        pg_conn.rollback()
    finally:
        mysql_cursor.close()
        mysql_conn.close()
        pg_cursor.close()
        pg_conn.close()

def sync_products():
    """同步商品数据"""
    print("开始同步商品数据...")
    
    # 连接 MySQL
    mysql_conn = pymysql.connect(**MYSQL_CONFIG)
    mysql_cursor = mysql_conn.cursor()
    
    # 连接 PostgreSQL
    pg_conn = psycopg2.connect(**PG_CONFIG)
    pg_cursor = pg_conn.cursor()
    
    try:
        # 清空现有数据
        pg_cursor.execute("TRUNCATE TABLE product_vectors")
        pg_conn.commit()
        print("已清空 product_vectors 表")
        
        # 读取商品数据
        mysql_cursor.execute("""
            SELECT id, product_name, product_code, specification 
            FROM product 
            WHERE is_deleted = 0 AND product_name IS NOT NULL
        """)
        
        products = mysql_cursor.fetchall()
        print(f"从 MySQL 读取到 {len(products)} 条商品数据")
        
        # 插入向量数据
        inserted = 0
        for product in products:
            product_id, product_name, product_code, specification = product
            
            # 生成向量（使用商品名称+规格）
            text = f"{product_name} {specification or ''}".strip()
            vector = build_vector(text)
            vector_str = '[' + ','.join([f'{v:.6f}' for v in vector]) + ']'
            
            # 插入 PostgreSQL
            pg_cursor.execute("""
                INSERT INTO product_vectors (product_id, product_name, product_code, specification, vector)
                VALUES (%s, %s, %s, %s, %s::vector(1024))
            """, (product_id, product_name, product_code or '', specification or '', vector_str))
            
            inserted += 1
            if inserted % 100 == 0:
                pg_conn.commit()
                print(f"已同步 {inserted} 条商品数据...")
        
        pg_conn.commit()
        print(f"商品数据同步完成，共 {inserted} 条")
        
    except Exception as e:
        print(f"同步商品数据出错: {e}")
        pg_conn.rollback()
    finally:
        mysql_cursor.close()
        mysql_conn.close()
        pg_cursor.close()
        pg_conn.close()

if __name__ == '__main__':
    print("=" * 50)
    print("向量数据同步工具")
    print("=" * 50)
    
    sync_customers()
    print()
    sync_products()
    
    print()
    print("=" * 50)
    print("同步完成！")
    print("=" * 50)
