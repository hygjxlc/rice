package com.bjdx.rice.admin.controller;

import com.bjdx.rice.admin.dto.RoleListReqDTO;
import com.bjdx.rice.admin.entity.Role;
import com.bjdx.rice.admin.service.RoleService;
import com.bjdx.rice.business.dto.ResponseObj;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@Api(tags = "角色管理")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/list/{roleName}")
    @ApiOperation("角色列表")
    public ResponseObj<Role> list(@RequestBody RoleListReqDTO dto) {
        return ResponseObj.success().put(roleService.listRoles(dto));
    }


    @GetMapping("/dropDown")
    @ApiOperation("下拉框角色列表")
    public ResponseObj dropDown() {
        return ResponseObj.success().put(roleService.dropDown());
    }
    @PostMapping("/save")
    @ApiOperation("新建角色")
    public ResponseObj save(@RequestBody Role role) {
        roleService.saveRole(role);
        return ResponseObj.success();
    }

    @GetMapping("/get/{id}")
    @ApiOperation("角色详情")
    public ResponseObj get(@PathVariable Long id) {
        return ResponseObj.success().put(roleService.getRoleWithPermissions(id));
    }

    // 获取所有可分配的权限（用于角色编辑页面）
    @GetMapping("/permissions")
    @ApiOperation("获取所有可分配的权限")
    public ResponseObj getAllPermissions() {
        return ResponseObj.success().put(roleService.getAllPermissions());
    }
}
