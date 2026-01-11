package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.FilmWithDetailsRowMapper;
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
import java.util.stream.Collectors;

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
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE film_id = ?
                """;

        int updatedRows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getRating() != null ? film.getRating().getId() : null,
                film.getId()
        );

        if (updatedRows == 0) {
            throw new EntityNotFoundException("Фильм", film.getId());
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public Optional<Film> getById(Long id) {
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

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        loadGenres(film);
        loadLikes(film);
        return Optional.of(film);
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
               m.code AS mpa_name,
               GROUP_CONCAT(DISTINCT g.genre_id) AS genre_ids,
               GROUP_CONCAT(DISTINCT g.name) AS genre_names,
               GROUP_CONCAT(DISTINCT fl.user_id) AS like_ids
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
        LEFT JOIN film_genres fg ON f.film_id = fg.film_id
        LEFT JOIN genres g ON fg.genre_id = g.genre_id
        LEFT JOIN film_likes fl ON f.film_id = fl.film_id
        GROUP BY f.film_id
        """;

        return jdbcTemplate.query(sql, new FilmWithDetailsRowMapper());
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
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
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int deletedRows = jdbcTemplate.update(sql, filmId, userId);

        if (deletedRows == 0) {
            throw new LikeOperationException("Пользователь не добавлял лайк к данному фильму");
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
        SELECT f.film_id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.mpa_id,
               m.code AS mpa_name,
               GROUP_CONCAT(DISTINCT g.genre_id) AS genre_ids,
               GROUP_CONCAT(DISTINCT g.name) AS genre_names,
               GROUP_CONCAT(DISTINCT fl.user_id) AS like_ids,
               COUNT(DISTINCT fl.user_id) AS likes_count
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
        LEFT JOIN film_genres fg ON f.film_id = fg.film_id
        LEFT JOIN genres g ON fg.genre_id = g.genre_id
        LEFT JOIN film_likes fl ON f.film_id = fl.film_id
        GROUP BY f.film_id
        ORDER BY likes_count DESC, f.film_id ASC
        LIMIT ?
        """;

        return jdbcTemplate.query(sql, new FilmWithDetailsRowMapper(), count);
    }
}
