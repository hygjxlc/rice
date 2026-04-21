package com.bjdx.rice.admin.mapper;

import com.bjdx.rice.admin.dto.UserListReqDTO;
import com.bjdx.rice.admin.dto.UserResDTO;
import com.bjdx.rice.admin.entity.Role;
import com.bjdx.rice.admin.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

@Repository
public interface UserMapper extends Mapper<User>,MySqlMapper<User> {
    User findByUsername(String username);
    List<Role> findRolesByUserId(Long userId);
    List<String> findPermissionsByUserId(Long userId);
    void deleteRolesByUserId(Long userId);
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
//    void deleteRolesByUsageId(Long id);
    List<User> selectAllWithoutPassword();
    List<Long> findRoleIdsByUserId(Long userId);

    List<UserResDTO> listUserWithRoles(@Param("dto") UserListReqDTO dto);
}
