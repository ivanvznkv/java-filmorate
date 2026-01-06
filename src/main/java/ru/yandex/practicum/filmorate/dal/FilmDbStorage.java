package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exception.LikeOperationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        String sql = """
        INSERT INTO films (name, description, release_date, duration, mpa_id)
        VALUES (?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null
                    ? Date.valueOf(film.getReleaseDate())
                    : null);
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getRating() != null
                    ? film.getRating().getId()
                    : null);
            return ps;
        }, keyHolder);

        long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);
        saveGenres(film);

        return getById(filmId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм", filmId));
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE film_id = ?
                """;

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getRating() != null ? film.getRating().getId() : null,
                film.getId()
        );

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return getById(film.getId())
                .orElseThrow(() -> new EntityNotFoundException("Фильм", film.getId()));
    }

    @Override
    public Optional<Film> getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("film_id не может быть null");
        }

        String sql = """
        SELECT f.film_id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.mpa_id,
               m.code AS mpa_name
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
        WHERE f.film_id = ?
        """;

        try {
            Film film = jdbcTemplate.queryForObject(sql, new FilmRowMapper(), id);
            loadGenres(film);
            loadLikes(film);
            return Optional.of(film);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    @Override
    public Collection<Film> getAll() {
        String sql = """
        SELECT f.film_id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.mpa_id,
               m.code AS mpa_name
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
        """;

        Collection<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());
        films.forEach(f -> {
            loadGenres(f);
            loadLikes(f);
        });
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private void loadGenres(Film film) {
        String sql = """
            SELECT g.genre_id,
                   g.name AS genre_name
            FROM genres g
            JOIN film_genres fg ON g.genre_id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.genre_id
            """;

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Genre(rs.getInt("genre_id"), rs.getString("genre_name")),
                film.getId());

        film.setGenres(genres);
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        Set<Long> likes = Set.copyOf(
                jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), film.getId())
        );
        film.setLikes(likes);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";

        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (DuplicateKeyException e) {
            throw new LikeOperationException("Пользователь уже ставил лайк этому фильму.");
        } catch (DataIntegrityViolationException e) {
            throw new LikeOperationException("Не удалось поставить лайк.");
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int deletedRows = jdbcTemplate.update(sql, filmId, userId);

        if (deletedRows == 0) {
            throw new LikeOperationException("Пользователь не добавлял лайк к данному фильму");
        }
    }
}
