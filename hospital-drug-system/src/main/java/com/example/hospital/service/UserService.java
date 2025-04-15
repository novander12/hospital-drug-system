package com.example.hospital.service;

import com.example.hospital.model.User;
import com.example.hospital.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return user != null && "ADMIN".equals(user.getRole());
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
     * 创建新用户
     * @param username 用户名
     * @param password 密码
     * @param role 角色
     * @return 保存后的用户对象
     */
    @Transactional
    public User createUser(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return createUser(user);
    }
    
    /**
     * 创建新用户
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    @Transactional
    public User createUser(User user) {
        // 加密密码
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        System.out.println("创建用户: " + user.getUsername());
        System.out.println("原始密码长度: " + user.getPassword().length());
        System.out.println("加密密码长度: " + encodedPassword.length());
        
        user.setPassword(encodedPassword);
        
        // 设置默认角色
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 更新用户角色
     * @param id 用户ID
     * @param role 角色
     * @return 更新后的用户对象
     */
    @Transactional
    public User updateUserRole(Long id, String role) {
        User user = findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
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
} 