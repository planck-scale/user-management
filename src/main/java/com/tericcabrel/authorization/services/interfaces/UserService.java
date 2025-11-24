package com.tericcabrel.authorization.services.interfaces;

import com.tericcabrel.authorization.exceptions.ResourceNotFoundException;
import com.tericcabrel.authorization.models.dtos.CreateUserDto;
import com.tericcabrel.authorization.models.dtos.ListAllUsersDto;
import com.tericcabrel.authorization.models.dtos.UpdatePasswordDto;
import com.tericcabrel.authorization.models.dtos.UpdateUserDto;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.response.ListAllUsersResponse;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User save(CreateUserDto createUserDto);

    List<User> findAllWithRole(String role);

    List<User> findAll();

    void delete(String id, String tenantId);

    User findByEmail(String email) throws ResourceNotFoundException;

    User findByEmailAndTenantId(String email, String tenantId) throws ResourceNotFoundException;

    User findById(String id) throws ResourceNotFoundException;

    User update(String id, UpdateUserDto updateUserDto) throws ResourceNotFoundException;

    void update(User user);

    User updatePassword(String id, UpdatePasswordDto updatePasswordDto)
        throws ResourceNotFoundException;

    void updatePassword(String id, String newPassword) throws ResourceNotFoundException;

    void confirm(String id) throws ResourceNotFoundException;

    ListAllUsersResponse findAll(ListAllUsersDto request, String tenantId);
}
