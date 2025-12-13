package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private long nextId = 1;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя отсутствует, использован логин как имя: {}", user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно добавлен: {}", user);
        return user;
    }

    @Override
    public User updateUser(User updatedUser) {
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
        return updatedUser;
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    private Long getNextId() {
        return nextId++;
    }
}
