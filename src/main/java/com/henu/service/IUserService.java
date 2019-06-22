package com.henu.service;

import com.henu.entity.User;
import com.henu.web.dto.UserDTO;

public interface IUserService {
    User findByUserName(String userName);

    ServiceResult<UserDTO> findById(Long userId);
}
