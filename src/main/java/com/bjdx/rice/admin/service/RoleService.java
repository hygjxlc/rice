package com.bjdx.rice.admin.service;

import com.bjdx.rice.admin.dto.DropDownDTO;
import com.bjdx.rice.admin.dto.RoleListReqDTO;
import com.bjdx.rice.admin.entity.Permission;
import com.bjdx.rice.admin.entity.Role;
import com.bjdx.rice.admin.mapper.PermissionMapper;
import com.bjdx.rice.admin.mapper.RoleMapper;
import com.bjdx.rice.business.dto.MyPage;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    public MyPage<Role> listRoles(RoleListReqDTO dto) {
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        MyPage<Role> page = new MyPage<>();
        PageHelper.startPage(pageNum, pageSize);
        List<Role> list = roleMapper.selectByRoleName(dto.getRoleName());
        if (list.isEmpty())
        {
            return page;
        }
        page = new MyPage<>(list);
        page.setList(list);
        return page;
    }

    public void saveRole(Role role) {
        if (role.getId() == null) {
            roleMapper.insert(role);
        } else {
            roleMapper.updateByPrimaryKeySelective(role);
            roleMapper.deletePermissionsByRoleId(role.getId());
        }
        if (role.getPermissionIds() != null) {
            for (Long pid : role.getPermissionIds()) {
                roleMapper.insertRolePermission(role.getId(), pid);
            }
        }
    }

    public List<Permission> getAllPermissions() {
        return permissionMapper.selectAll();
    }

    public Role getRoleWithPermissions(Long roleId) {
        Role role = roleMapper.selectByPrimaryKey(roleId);
        if (role != null) {
            role.setPermissionIds(
                    roleMapper.findPermissionsByRoleId(roleId).stream()
                            .map(Permission::getId)
                            .collect(Collectors.toList())
            );
        }
        return role;
    }

    public List<DropDownDTO> dropDown() {
        return roleMapper.dropDown();
    }
}