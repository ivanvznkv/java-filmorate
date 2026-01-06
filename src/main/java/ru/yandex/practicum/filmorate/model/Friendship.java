package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = { "userId", "friendId" })
public class Friendship {
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
}
