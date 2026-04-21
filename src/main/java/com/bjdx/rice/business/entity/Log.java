package com.bjdx.rice.business.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.io.Serializable;

/**
 * (Log)实体类
 *
 * @author makejava
 * @since 2026-03-22 18:01:24
 */
@Data
@Entity
@Table(name = "log")
public class Log implements Serializable {
    private static final long serialVersionUID = -23402972759876927L;

    @Id
    private Integer id;
/**
     * 类型
     */
    private String type;
/**
     * 内容
     */
    private String content;
/**
     * 时间
     */
    private Date createTime;
/**
     * 创建人
     */
    private String createUser;



}

