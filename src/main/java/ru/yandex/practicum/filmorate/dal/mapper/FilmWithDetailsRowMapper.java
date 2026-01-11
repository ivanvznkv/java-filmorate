package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class FilmWithDetailsRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(rs.getInt("duration"));

        Long mpaId = rs.getLong("mpa_id");
        if (!rs.wasNull()) {
            MpaRating rating = new MpaRating();
            rating.setId(mpaId);
            rating.setName(rs.getString("mpa_name"));
            film.setRating(rating);
        }

        String genreIdsStr = rs.getString("genre_ids");
        if (genreIdsStr != null && !genreIdsStr.isEmpty()) {
            List<Genre> genres = new ArrayList<>();
            String[] ids = genreIdsStr.split(",");
            String[] names = rs.getString("genre_names").split(",");

            for (int i = 0; i < ids.length; i++) {
                if (!ids[i].isEmpty()) {
                    genres.add(new Genre(
                            Integer.parseInt(ids[i]),
                            i < names.length ? names[i] : ""
                    ));
                }
            }
            film.setGenres(genres);
        } else {
            film.setGenres(new ArrayList<>());
        }

        String likeIdsStr = rs.getString("like_ids");
        if (likeIdsStr != null && !likeIdsStr.isEmpty()) {
            Set<Long> likes = Arrays.stream(likeIdsStr.split(","))
                    .filter(id -> !id.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            film.setLikes(likes);
        } else {
            film.setLikes(new HashSet<>());
        }
        return film;
    }
}
