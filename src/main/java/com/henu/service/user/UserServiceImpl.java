package com.henu.service.user;

import com.henu.entity.User;
import com.henu.repository.UserRepository;
import com.henu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User findByUserName(String userName) {
        User user=userRepository.findByName(userName);
        return user;
    }
}
