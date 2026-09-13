package org.txf.myblogsprinboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.txf.myblogsprinboot.dao.UserMapper;
import org.txf.myblogsprinboot.model.User;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public User getUserByUserName(String username) {
        return userMapper.selectUserByUsername(username);
    }
}
