package com.bjdx.rice.admin.service;

import com.bjdx.rice.admin.dto.UserListReqDTO;
import com.bjdx.rice.admin.dto.UserResDTO;
import com.bjdx.rice.admin.entity.User;
import com.bjdx.rice.admin.mapper.RoleMapper;
import com.bjdx.rice.admin.mapper.UserMapper;
import com.bjdx.rice.business.dto.MyPage;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

//    public User login(String username, String password) {
//        User user = userMapper.findByUsername(username);
//        if (user == null || user.getStatus() != 1) return null;
//        if (passwordEncoder.matches(password, user.getPassword())) {
//            return user;
//        }
//        return null;
//    }

    public MyPage<UserResDTO> listUsers(UserListReqDTO dto) {
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();
        MyPage<UserResDTO> page = new MyPage<>();
        PageHelper.startPage(pageNum, pageSize);
        List<UserResDTO> list = userMapper.listUserWithRoles(dto);
        if (list.isEmpty())
        {
            return page;
        }
        page = new MyPage<>(list);
        page.setList(list);
        return page;
    }

    public void saveUser(User user) {
        user.setCreateTime(new Date());
        if (user.getId() == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userMapper.insert(user);
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userMapper.updateByPrimaryKeySelective(user);
            userMapper.deleteRolesByUserId(user.getId());
        }
        if (user.getRoleIds() != null) {
            for (Long roleId : user.getRoleIds()) {
                userMapper.insertUserRole(user.getId(), roleId);
            }
        }
    }

    public void deleteUser(Long id) {
        userMapper.deleteByPrimaryKey(id);
        userMapper.deleteRolesByUserId(id);
    }
}
