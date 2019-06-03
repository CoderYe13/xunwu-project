package com.henu.entity;

import com.henu.ApplicationTests;
import com.henu.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class UserRepositoryTest extends ApplicationTests {
    @Autowired
    private UserRepository userRepository;
    @Test
    public void testFindOne(){
         userRepository.findAll();
    }
}
