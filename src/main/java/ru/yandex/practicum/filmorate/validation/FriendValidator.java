package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exception.FriendOperationException;

public class FriendValidator {
    private FriendValidator() {
    }

    public static void validateAddFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new FriendOperationException("Добавить самого себя в друзья невозможно.");
        }
    }
}
