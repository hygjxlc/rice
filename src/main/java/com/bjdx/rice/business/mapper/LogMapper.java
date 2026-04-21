package com.bjdx.rice.business.mapper;

import com.bjdx.rice.business.entity.Log;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.MySqlMapper;

@Repository
public interface LogMapper extends BaseMapper<Log>, MySqlMapper<Log> {
}
