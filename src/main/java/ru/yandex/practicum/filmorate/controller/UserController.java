package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@Validated(ValidationGroups.OnCreate.class) @RequestBody User user) {
        log.info("Получен запрос на добавление пользователя: {}", user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя отсутствует, использован логин как имя: {}", user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Validated(ValidationGroups.OnUpdate.class) @RequestBody User updatedUser) {
        log.info("Получен запрос на обновление пользователя: {}", updatedUser);

        if (updatedUser.getId() == null || !users.containsKey(updatedUser.getId())) {
            log.warn("Ошибка обновления: пользователь с id={} не найден", updatedUser.getId());
            throw new EntityNotFoundException("Пользователь", updatedUser.getId());
        }

        User oldUser = users.get(updatedUser.getId());

        if (updatedUser.getName() != null) {
            oldUser.setName(updatedUser.getName().isBlank() ? updatedUser.getLogin() : updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            oldUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getLogin() != null) {
            oldUser.setLogin(updatedUser.getLogin());
        }
        if (updatedUser.getBirthday() != null) {
            oldUser.setBirthday(updatedUser.getBirthday());
        }

        log.info("Пользователь успешно обновлён: {}", oldUser);
        return oldUser;
    }

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Запрошен список всех пользователей.");
        return users.values();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
