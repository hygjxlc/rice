package com.bjdx.rice.admin.mapper;

import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.admin.entity.Permission;
import com.bjdx.rice.admin.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

@Repository
public interface RoleMapper extends Mapper<Role>,MySqlMapper<Role> {
    List<Permission> findPermissionsByRoleId(Long roleId);
    void deletePermissionsByRoleId(Long roleId);
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    List<DropDownDTO> dropDown();

    List<Role> selectByRoleName(String roleName);
}
