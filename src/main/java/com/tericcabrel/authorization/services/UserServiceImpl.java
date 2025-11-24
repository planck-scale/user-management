package com.tericcabrel.authorization.services;

import com.tericcabrel.authorization.exceptions.ResourceNotFoundException;
import com.tericcabrel.authorization.models.dtos.*;
import com.tericcabrel.authorization.models.entities.Role;
import com.tericcabrel.authorization.models.entities.User;
import com.tericcabrel.authorization.models.response.ListAllUsersResponse;
import com.tericcabrel.authorization.repositories.UserRepository;
import com.tericcabrel.authorization.services.interfaces.RoleService;
import com.tericcabrel.authorization.services.interfaces.UserService;
import com.tericcabrel.authorization.utils.AuthUtils;
import com.tericcabrel.authorization.utils.FilterCriteriaBuilder;
import com.tericcabrel.authorization.utils.SearchUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.tericcabrel.authorization.utils.Constants.USER_NOT_FOUND_MESSAGE;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    protected MongoTemplate mongodb;

    @Autowired
    private PasswordEncoder bCryptEncoder; // Fails when injected by the constructor

    @Override
    public User save(CreateUserDto createUserDto) {
        User newUser = new User();

        newUser.setEmail(createUserDto.getEmail())
                .setFirstName(createUserDto.getFirstName())
                .setLastName(createUserDto.getLastName())
                .setPassword(bCryptEncoder.encode(createUserDto.getPassword()))
                .setGender(createUserDto.getGender())
                .setConfirmed(createUserDto.isConfirmed())
                .setTenantId(createUserDto.getTenantId())
                .setEnabled(createUserDto.isEnabled())
                .setAvatar(null)
                .setTimezone(createUserDto.getTimezone())
                .setCoordinates(createUserDto.getCoordinates())
                .setRole(createUserDto.getRole())
                .setPhoneNumber(createUserDto.getPhoneNumber())
                .setAttributes(createUserDto.getAttributes());

        return userRepository.save(newUser);
    }

    @Override
    public List<User> findAllWithRole(String role) {
        List<User> list = new ArrayList<>();
        User probe = new User();
        Role r = new Role();
        r.setName(role);
        probe.setRole(r);

        Example<User> example = Example.of(probe);
        userRepository.findAll(example).iterator().forEachRemaining(list::add);
        return list; //todo
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    public ListAllUsersResponse findAll(ListAllUsersDto request, String tenantId) {
        log.debug("serving request {}", request);
        if(Objects.nonNull(tenantId)) {
            FilterCondition tenant = new FilterCondition();
            tenant.setValue(tenantId);
            tenant.setField("tenantId");
            tenant.setOperator("eq");
            request.getFilters().getConditions().add(tenant);
            //log.debug("serving modified request {}", request);
        }

        Sort sort = SearchUtils.buildSort(request.getSort());
        Pageable pageable = SearchUtils.buildPageable(request.getPage(), request.getSize(), sort);
        Query query = FilterCriteriaBuilder.buildDynamicQuery(request.getFilters(), request.getFields());
        query.with(pageable);

        List users = mongodb.find(query, User.class);
        long count = mongodb.count(Query.of(query).limit(-1).skip(-1), User.class);
        Page<User> usersPage = PageableExecutionUtils.getPage(users, pageable, () -> count);

        ListAllUsersResponse response = new ListAllUsersResponse();
        response.setUsers(users);
        response.setTotalElements(usersPage.getTotalElements());
        response.setTotalPages(usersPage.getTotalPages());
        response.setCurrentPage(usersPage.getNumber());
        return response;
    }

    @Override
    public void delete(String id, String tenantId) {
        try {
            User user = null;
            if(Objects.nonNull(tenantId)) {
                user = findByEmailAndTenantId(id, tenantId);
            } else {
                user = findByEmail(id);
            }
            user.setDeleted(true); // soft delete this time
            userRepository.save(user);
        } catch (Exception e) {
            log.warn("unable to delete {}", e.getMessage());
        }
    }

    @Override
    public User findByEmail(String email) throws ResourceNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE);
        }
        return optionalUser.get();
    }

    @Override
    public User findByEmailAndTenantId(String email, String tenantId) throws ResourceNotFoundException {

        Optional<User> optionalUser = userRepository.findByEmailAndTenantId(email, tenantId);
        return optionalUser.orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    @Override
    public User findById(String id) throws ResourceNotFoundException {
        Optional<User> optionalUser = userRepository.findById(new ObjectId(id));

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE);
        }

        return optionalUser.get();
    }

    @Override
    public User update(String id, UpdateUserDto updateUserDto) throws ResourceNotFoundException {

        String tenantId = AuthUtils.getTenantId();
        User user = null;
        if(Objects.nonNull(tenantId)) {
            user = findByEmailAndTenantId(id, tenantId);
        } else {
            user = findByEmail(id);
        }

        if(updateUserDto.getFirstName() != null) {
            user.setFirstName(updateUserDto.getFirstName());
        }
        if(updateUserDto.getLastName() != null) {
            user.setLastName(updateUserDto.getLastName());
        }
        if(updateUserDto.getTimezone() != null) {
            user.setTimezone(updateUserDto.getTimezone());
        }
        if(updateUserDto.getGender() != null) {
            user.setGender(updateUserDto.getGender());
        }
        if(updateUserDto.getAvatar() != null) {
            user.setAvatar(updateUserDto.getAvatar());
        }
        if(updateUserDto.getCoordinates() != null) {
            user.setCoordinates(updateUserDto.getCoordinates());
        }

        if(Objects.nonNull(updateUserDto.getConfirmed())) {
            user.setConfirmed(updateUserDto.getConfirmed());
        }

        if(Objects.nonNull(updateUserDto.getEnabled())) {
            user.setEnabled(updateUserDto.getEnabled());
        }

        return userRepository.save(user);
    }

    @Override
    public void update(User user) {
        userRepository.save(user);
    }

    @Override
    public User updatePassword(String id, UpdatePasswordDto updatePasswordDto) throws ResourceNotFoundException {
        User user = findById(id);
        if (bCryptEncoder.matches(updatePasswordDto.getCurrentPassword(), user.getPassword())) {
            user.setPassword(bCryptEncoder.encode(updatePasswordDto.getNewPassword()));
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public void updatePassword(String id, String newPassword) throws ResourceNotFoundException {
        User user = findById(id);
        user.setPassword(bCryptEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void confirm(String id) throws ResourceNotFoundException {
        User user = findById(id);
        user.setConfirmed(true);
        userRepository.save(user);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(username);
        if(userOptional.isEmpty()){
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        User user = userOptional.get();
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), user.getPassword(), user.isEnabled(), true, true, user.isConfirmed(), getAuthority(user)
        );
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));
        user.allPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));
        return authorities;
    }
}
