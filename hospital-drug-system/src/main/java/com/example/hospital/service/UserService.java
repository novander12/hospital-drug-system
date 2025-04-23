package com.example.hospital.service;

import com.example.hospital.dto.UserDTO;
import com.example.hospital.model.User;
import com.example.hospital.model.Role;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.dto.PasswordChangeRequestDTO;
import com.example.hospital.dto.AdminPasswordResetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 找到的用户，如果不存在则返回null
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 验证用户密码是否正确
     * @param username 用户名
     * @param password 密码
     * @return 如果验证成功返回用户对象，否则返回null
     */
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        System.out.println("验证用户: " + username);
        System.out.println("找到用户: " + (user != null ? user.toString() : "null"));
        
        if (user != null) {
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("密码匹配结果: " + matches);
            System.out.println("数据库密码长度: " + user.getPassword().length());
            if (matches) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * 检查用户是否有管理员权限
     * @param username 用户名
     * @return 如果是管理员返回true，否则返回false
     */
    public boolean isAdmin(String username) {
        User user = findByUsername(username);
        return user != null && user.getRole() == Role.ADMIN;
    }
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 如果存在返回true，否则返回false
     */
    public boolean userExists(String username) {
        return findByUsername(username) != null;
    }
    
    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 找到的用户，如果不存在则返回null
     */
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 获取所有用户，并转换为 UserDTO 列表
     * @return UserDTO 列表
     */
    public List<UserDTO> findAllUserDTOs() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建新用户 (重载以接收 Role 枚举)
     * @param username 用户名
     * @param password 密码
     * @param role 角色枚举
     * @return 保存后的用户对象
     */
    @Transactional
    public User createUser(String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Ensure password is encoded
        user.setRole(role != null ? role : Role.USER); // Set role, default to USER if null
        return userRepository.save(user);
    }
    
    /**
     * 更新用户角色 (重载以接收 Role 枚举)
     * @param id 用户ID
     * @param role 角色枚举
     * @return 更新后的用户对象
     */
    @Transactional
    public User updateUserRole(Long id, Role role) {
        User user = findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (role == null) {
            // 或者抛出异常，取决于业务逻辑是否允许角色为null
            throw new IllegalArgumentException("角色不能为空"); 
        }
        user.setRole(role);
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     * @param id 用户ID
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        userRepository.deleteById(id);
    }

    /**
     * 修改用户密码
     * @param username 用户名
     * @param passwordChangeRequest 包含旧密码和新密码的DTO
     * @throws RuntimeException 如果用户不存在或旧密码错误
     */
    @Transactional
    public void changePassword(String username, PasswordChangeRequestDTO passwordChangeRequest) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }

        // 验证通过，设置新密码
        String encodedNewPassword = passwordEncoder.encode(passwordChangeRequest.getNewPassword());
        user.setPassword(encodedNewPassword);

        // 保存更新
        userRepository.save(user);
        
        // 可选: 记录密码修改操作日志
        // operationLogService.logPasswordChange(username);
    }

    /**
     * 由管理员重置指定用户的密码
     * @param userId 要重置密码的用户ID
     * @param newPassword 新密码
     * @throws RuntimeException 如果用户不存在
     */
    @Transactional
    public void resetPasswordByAdmin(Long userId, String newPassword) {
        User user = findById(userId); // Use findById for admin actions
        if (user == null) {
            throw new RuntimeException("用户不存在，ID: " + userId);
        }

        // 直接设置新密码，无需验证旧密码
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        // 保存更新
        userRepository.save(user);
        
        // 可选: 记录密码重置操作日志
        // operationLogService.logAdminPasswordReset(adminUsername, targetUsername);
    }

    /**
     * 将 User 实体转换为 UserDTO
     * @param user User 实体
     * @return UserDTO 对象
     */
    public UserDTO convertToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getRole() != null ? user.getRole().name() : null, // Convert enum to string
                user.getRealName(),
                user.getDepartment(),
                user.getEmail(),
                user.getPhone()
        );
    }
} 