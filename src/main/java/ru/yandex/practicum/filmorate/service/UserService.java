package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FriendValidator;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

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

        getById(userId);
        getById(friendId);

        friendshipStorage.addFriend(userId, friendId);

        log.info("Пользователь с id={} добавил в друзья пользователя с id={}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        getById(userId);
        getById(friendId);

        friendshipStorage.removeFriend(userId, friendId);

        log.info("Пользователь с id={} удалил из друзей пользователя с id={}", userId, friendId);
    }

    public Collection<User> getFriends(long userId) {
        getById(userId);
        return friendshipStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        getById(userId);
        getById(friendId);

        return friendshipStorage.getCommonFriends(userId, friendId).stream().toList();
    }
}
