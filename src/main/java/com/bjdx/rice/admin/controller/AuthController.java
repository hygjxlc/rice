package com.bjdx.rice.admin.controller;

import com.bjdx.rice.admin.dto.LoginDTO;
import com.bjdx.rice.admin.mapper.UserMapper;
import com.bjdx.rice.admin.service.JwtUtil;
import com.bjdx.rice.business.dto.ResponseObj;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "认证")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public ResponseObj login(@RequestBody LoginDTO dto) {
        // 添加参数验证，防止空值传递给认证管理器
        if (dto == null ||
                dto.getUsername() == null ||
                dto.getPassword() == null ||
                dto.getUsername().trim().isEmpty() ||
                dto.getPassword().trim().isEmpty()) {
            return ResponseObj.error("用户名或密码不能为空");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername().trim(),
                            dto.getPassword().trim()
                    )
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            // 获取权限列表（用于前端控制 UI）
            List<String> permissions = userMapper.findPermissionsByUserId(
                    userMapper.findByUsername(dto.getUsername()).getId()
            );

            String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("permissions", permissions);

            return ResponseObj.success().put(resp);
        } catch (Exception e) {
            return ResponseObj.error("用户名或密码错误");
        }
    }


}
