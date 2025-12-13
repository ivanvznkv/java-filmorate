package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FriendValidator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getById(long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", id));
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(long userId, long friendId) {
        FriendValidator.validateAddFriend(userId, friendId);

        User user = userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", friendId));
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
        log.info("Пользователи с id={} и id={} теперь друзья", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", friendId));

        if (!user.getFriends().contains(friendId)) {
            log.warn("Попытка удалить несуществующего друга: userId={} friendId={}", userId, friendId);
            return;
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь с id={} удалил из друзей пользователя с id={}", userId, friendId);
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", friendId));

        Set<Long> commonIds = user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .collect(Collectors.toSet());
        log.info(
                "Общие друзья пользователей {} и {}: {}",
                userId, friendId, commonIds
        );
        return commonIds.stream()
                .map(id -> userStorage.getById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Пользователь", id)))
                .collect(Collectors.toList());
    }
}
