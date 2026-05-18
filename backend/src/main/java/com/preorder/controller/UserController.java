package com.preorder.controller;

import com.preorder.common.Result;
import com.preorder.entity.User;
import com.preorder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = userService.getUser(1L);
        }
        return Result.success("登录成功", user);
    }

    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        try {
            User created = userService.createUser(user);
            return Result.success("创建成功", created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/recharge")
    public Result<User> recharge(@PathVariable Long id, @RequestBody Map<String, BigDecimal> params) {
        try {
            BigDecimal amount = params.get("amount");
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("充值金额必须大于0");
            }
            User user = userService.updateBalance(id, amount);
            return Result.success("充值成功", user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
