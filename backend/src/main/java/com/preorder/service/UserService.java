package com.preorder.service;

import com.preorder.entity.User;
import com.preorder.store.InMemoryStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    public List<User> getAllUsers() {
        return new java.util.ArrayList<>(InMemoryStore.USER_STORE.values());
    }

    public User getUser(Long id) {
        return InMemoryStore.getUser(id);
    }

    public User getUserByUsername(String username) {
        return InMemoryStore.USER_STORE.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public User createUser(User user) {
        user.setId(InMemoryStore.USER_ID_GENERATOR.getAndIncrement());
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        user.setCreateTime(LocalDateTime.now());
        InMemoryStore.USER_STORE.put(user.getId(), user);
        return user;
    }

    public User updateBalance(Long id, BigDecimal amount) {
        User user = InMemoryStore.getUser(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setBalance(user.getBalance().add(amount));
        return user;
    }
}
