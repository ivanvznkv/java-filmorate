package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FriendValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final UserValidator userValidator;

    public void addFriend(long userId, long friendId) {
        FriendValidator.validateAddFriend(userId, friendId);

        User user = userValidator.getUserOrThrow(userId);
        User friend = userValidator.getUserOrThrow(friendId);
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Пользователи с id={} и id={} теперь друзья", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userValidator.getUserOrThrow(userId);
        User friend = userValidator.getUserOrThrow(friendId);

        if (!user.getFriends().contains(friendId)) {
            log.warn("Попытка удалить несуществующего друга: userId={} friendId={}", userId, friendId);
            return;
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Пользователь с id={} удалил из друзей пользователя с id={}", userId, friendId);
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        User user = userValidator.getUserOrThrow(userId);
        User friend = userValidator.getUserOrThrow(friendId);

        Set<Long> commonIds = user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .collect(Collectors.toSet());
        log.info(
                "Общие друзья пользователей {} и {}: {}",
                userId, friendId, commonIds
        );
        return commonIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }
}
