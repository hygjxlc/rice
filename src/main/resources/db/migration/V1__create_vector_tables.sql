-- PGVector 向量数据库初始化脚本
-- 执行前请确保已安装 pgvector 扩展: CREATE EXTENSION vector;

-- ============================================
-- 客户向量表
-- ============================================
CREATE TABLE IF NOT EXISTS customer_vectors (
    id BIGINT PRIMARY KEY,
    unit_name VARCHAR(255) NOT NULL,
    unit_alias VARCHAR(255),
    vector VECTOR(768),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 客户向量表索引
CREATE INDEX IF NOT EXISTS idx_customer_vector ON customer_vectors 
USING ivfflat (vector vector_cosine_ops) WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_customer_name ON customer_vectors (unit_name);

COMMENT ON TABLE customer_vectors IS '客户向量表，存储客户名称的向量表示';
COMMENT ON COLUMN customer_vectors.id IS '客户ID，与 customer_info 表关联';
COMMENT ON COLUMN customer_vectors.unit_name IS '客户名称';
COMMENT ON COLUMN customer_vectors.unit_alias IS '客户别名';
COMMENT ON COLUMN customer_vectors.vector IS '向量表示，维度768';
COMMENT ON COLUMN customer_vectors.metadata IS '元数据JSON';

-- ============================================
-- 商品向量表
-- ============================================
CREATE TABLE IF NOT EXISTS product_vectors (
    id BIGINT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    product_code VARCHAR(100),
    vector VECTOR(768),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品向量表索引
CREATE INDEX IF NOT EXISTS idx_product_vector ON product_vectors 
USING ivfflat (vector vector_cosine_ops) WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_product_name ON product_vectors (product_name);

COMMENT ON TABLE product_vectors IS '商品向量表，存储商品名称的向量表示';
COMMENT ON COLUMN product_vectors.id IS '商品ID，与 product 表关联';
COMMENT ON COLUMN product_vectors.product_name IS '商品名称';
COMMENT ON COLUMN product_vectors.product_code IS '商品编码';
COMMENT ON COLUMN product_vectors.vector IS '向量表示，维度768';
COMMENT ON COLUMN product_vectors.metadata IS '元数据JSON';

-- ============================================
-- 向量匹配日志表（可选）
-- ============================================
CREATE TABLE IF NOT EXISTS vector_match_logs (
    id BIGSERIAL PRIMARY KEY,
    match_type VARCHAR(20) NOT NULL,  -- CUSTOMER / PRODUCT
    query_text VARCHAR(500) NOT NULL,
    matched_id BIGINT,
    matched_name VARCHAR(255),
    similarity_score FLOAT,
    threshold FLOAT,
    is_success BOOLEAN,
    execution_time_ms INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_match_logs_type ON vector_match_logs (match_type);
CREATE INDEX IF NOT EXISTS idx_match_logs_time ON vector_match_logs (created_at);

COMMENT ON TABLE vector_match_logs IS '向量匹配日志表，记录匹配过程';

-- ============================================
-- 向量同步任务表（用于全量重建）
-- ============================================
CREATE TABLE IF NOT EXISTS vector_sync_tasks (
    id BIGSERIAL PRIMARY KEY,
    task_type VARCHAR(20) NOT NULL,  -- CUSTOMER / PRODUCT
    task_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING / RUNNING / COMPLETED / FAILED
    total_count INTEGER DEFAULT 0,
    processed_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE vector_sync_tasks IS '向量同步任务表，记录全量重建进度';
