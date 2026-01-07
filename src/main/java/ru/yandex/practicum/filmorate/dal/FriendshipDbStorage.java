package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public void addFriend(long userId, long friendId) {
        String sql = """
                INSERT INTO friendships (user_id, friend_id, status)
                VALUES (?, ?, 'CONFIRMED')
                """;
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        String sql = """
                DELETE FROM friendships
                WHERE user_id = ? AND friend_id = ?
                """;
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(long userId) {
        String sql = """
                SELECT u.*
                FROM users u
                JOIN friendships f ON u.user_id = f.friend_id
                WHERE f.user_id = ?
                """;

        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long friendId) {
        String sql = """
                SELECT u.*
                FROM users u
                JOIN friendships f1 ON u.user_id = f1.friend_id
                JOIN friendships f2 ON u.user_id = f2.friend_id
                WHERE f1.user_id = ?
                  AND f2.user_id = ?
                """;

        return jdbcTemplate.query(sql, userRowMapper, userId, friendId);
    }
}
