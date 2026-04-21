package com.bjdx.rice.admin.mapper;

import com.bjdx.rice.admin.entity.Permission;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

@Repository
public interface PermissionMapper extends Mapper<Permission>, MySqlMapper<Permission> {
}