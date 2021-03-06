package com.henu.service.user;

import com.henu.entity.Role;
import com.henu.entity.User;
import com.henu.repository.RoleRepository;
import com.henu.repository.UserRepository;
import com.henu.service.IUserService;
import com.henu.service.ServiceResult;
import com.henu.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public User findByUserName(String userName) {
        User user = userRepository.findByName(userName);

        if (user == null) {
            return null;
        }

        List<Role> roles = roleRepository.findRolesByUserId(user.getId());

        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("非法权限");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        System.out.println(authorities);
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        User user=userRepository.findById(userId).get();
        if (user==null){
            return ServiceResult.notFound();
        }
        UserDTO userDTO=modelMapper.map(user,UserDTO.class);
        return ServiceResult.of(userDTO);
    }
}
