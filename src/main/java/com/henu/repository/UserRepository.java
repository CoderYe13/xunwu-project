package com.henu.repository;

import com.henu.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByName(String userName);
}
