package org.txf.myblogsprinboot.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.txf.myblogsprinboot.model.User;

@Mapper
public interface UserMapper {
    User selectUserByUsername(@Param("username")String username);

    User selectUserByUserId(@Param("id")Long id);
}
