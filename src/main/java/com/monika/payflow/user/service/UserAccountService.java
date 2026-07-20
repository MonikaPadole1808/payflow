package com.monika.payflow.user.service;

import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.user.dto.UserAdminResponse;
import com.monika.payflow.user.repository.UserRepository;
import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.ConflictException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccountService {

    private final UserRepository userRepository;

    public UserAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createActiveUser(String email, String encodedPassword) {
        User user = new User(email, encodedPassword, UserRole.USER, UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> getUsersForAdmin(String search) {
        return userRepository.findAll()
                .stream()
                .filter(user -> matchesSearch(user, search))
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUserForAdmin(UUID userId) {
        return toAdminResponse(findUserById(userId));
    }

    @Transactional
    public UserAdminResponse updateUserForAdmin(UUID userId, String email, UserStatus status) {
        User user = findUserById(userId);

        if (email != null && !email.isBlank()) {
            String normalizedEmail = email.trim().toLowerCase();
            if (!normalizedEmail.equals(user.email()) && userRepository.existsByEmail(normalizedEmail)) {
                throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS.defaultMessage(), ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.updateEmail(normalizedEmail);
        }

        if (status != null) {
            user.updateStatus(status);
        }

        return toAdminResponse(user);
    }

    @Transactional
    public UserAdminResponse activateUser(UUID userId) {
        User user = findUserById(userId);
        user.updateStatus(UserStatus.ACTIVE);
        return toAdminResponse(user);
    }

    @Transactional
    public UserAdminResponse deactivateUser(UUID userId) {
        User user = findUserById(userId);
        user.updateStatus(UserStatus.DISABLED);
        return toAdminResponse(user);
    }

    @Transactional
    public UserAdminResponse updateUserRole(UUID userId, UserRole role) {
        User user = findUserById(userId);
        user.updateRole(role);
        return toAdminResponse(user);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countBlockedUsers() {
        return userRepository.countByStatus(UserStatus.DISABLED);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage(),
                        ErrorCode.RESOURCE_NOT_FOUND
                ));
    }

    private boolean matchesSearch(User user, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String normalizedSearch = search.trim().toLowerCase();
        return user.email().toLowerCase().contains(normalizedSearch)
                || user.id().toString().contains(normalizedSearch)
                || user.role().name().toLowerCase().contains(normalizedSearch)
                || user.status().name().toLowerCase().contains(normalizedSearch);
    }

    private UserAdminResponse toAdminResponse(User user) {
        return new UserAdminResponse(user.id(), user.email(), user.role(), user.status());
    }
}
