package ru.kata.spring.boot_security.demo.services;

import jakarta.annotation.PostConstruct;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.kata.spring.boot_security.demo.exceptions.UserNotFoundException;
import ru.kata.spring.boot_security.demo.exceptions.UsernameExistException;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return user;
    }

    @Override
    public void editUser(long id, User user) {
        // Проверяем существование пользователя
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        // Обновляем поля, сохраняя оригинальный ID
        user.setUserId(id);

        // Проверяем пароль
        String oldPassword = existingUser.getPassword();
        String newPassword = user.getPassword();

        if (!oldPassword.equals(newPassword)) {
            // Если пароль изменился
            validateUsernameUniqueness(user);
            // Хешируем новый пароль
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // Сохраняем изменения
        userRepository.save(user);
    }

    private void validateUsernameUniqueness(User user) {
        if (findByUsername(user.getUsername()) != null) {
            throw new UsernameExistException("Пользователь с таким логином уже существует");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(long id) {
        User user = null;
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            user = optional.get();
        }
        return user;
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(encodePassword(user));
    }

    @Override
    public void update(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @PostConstruct
    public void addDefaultUser() {

        Set<Role> roleSetAdminAndUser = new HashSet<>();
        roleSetAdminAndUser.add(roleRepository.findById(1L).orElse(null));
        roleSetAdminAndUser.add(roleRepository.findById(2L).orElse(null));

        Set<Role> roleSetUser = new HashSet<>();
        roleSetUser.add(roleRepository.findById(2L).orElse(null));

        User usrAdmin = new User("Admin", "Admin", (byte) 30, "admin@mail.com", "12345", roleSetAdminAndUser);
        User usrUser = new User("Алексей", "Сидоров", (byte) 25, "alex@mail.com", "12345", roleSetUser);

        saveUser(usrAdmin);
        saveUser(usrUser);
    }
}

