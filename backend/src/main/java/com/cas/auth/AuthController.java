package com.cas.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.auth.entity.User;
import com.cas.auth.mapper.UserMapper;
import com.cas.common.exception.BusinessException;
import com.cas.common.result.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(409, "账号已被禁用");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getRole());

        return ApiResponse.success(LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .role(user.getRole())
                        .department(user.getDepartment())
                        .major(user.getMajor())
                        .grade(user.getGrade())
                        .build())
                .build());
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        return ApiResponse.success(LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .department(user.getDepartment())
                .major(user.getMajor())
                .grade(user.getGrade())
                .build());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String jti = jwtTokenProvider.getJtiFromToken(token);
            long ttl = jwtTokenProvider.getRemainingTtlSeconds(token);
            if (ttl > 0) {
                redisTemplate.opsForValue().set("cas:token:blacklist:" + jti, "1", ttl, TimeUnit.SECONDS);
            }
        }
        return ApiResponse.success();
    }
}