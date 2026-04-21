package com.bjdx.rice.admin.controller;

import com.bjdx.rice.admin.dto.UserListReqDTO;
import com.bjdx.rice.admin.dto.UserResDTO;
import com.bjdx.rice.admin.entity.User;
import com.bjdx.rice.admin.service.UserService;
import com.bjdx.rice.business.dto.ResponseObj;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/list")
    @ApiOperation("用户列表")
    public ResponseObj<UserResDTO> list(@RequestBody UserListReqDTO dto) {
        return ResponseObj.success().put(userService.listUsers(dto));
    }

    @PostMapping("/save")
    @ApiOperation("新建、编辑用户")
    public ResponseObj save(@RequestBody User user) {
        userService.saveUser(user);
        return ResponseObj.success();
    }

    @GetMapping("/delete/{id}")
    @ApiOperation("删除用户")
    public ResponseObj delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseObj.success();
    }
}