package com.henu.service;

import com.henu.entity.User;

public interface IUserService {
    User findByUserName(String userName);
}
