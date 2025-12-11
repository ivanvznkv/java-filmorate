package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    // --- STORAGE операции
    @PostMapping
    public User addUser(@Validated(ValidationGroups.OnCreate.class) @RequestBody User user) {
        log.debug("POST /users {}", user);
        return userStorage.addUser(user);
    }

    @PutMapping
    public User updateUser(@Validated(ValidationGroups.OnUpdate.class) @RequestBody User updatedUser) {
        log.debug("PUT /users {}", updatedUser);
        return userStorage.updateUser(updatedUser);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        log.debug("GET /users/{}", id);
        return userStorage.getById(id);
    }

    @GetMapping
    public Collection<User> getAll() {
        log.debug("GET /users");
        return userStorage.getAll();
    }

    // --- SERVICE операции
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("PUT /users/{}/friends/{}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("DELETE /users/{}/friends/{}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        log.debug("GET /users/{}/friends", id);
        return userStorage.getById(id).getFriends().stream()
                .map(userStorage::getById)
                .toList();
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("GET /users/{}/friends/common/{}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
