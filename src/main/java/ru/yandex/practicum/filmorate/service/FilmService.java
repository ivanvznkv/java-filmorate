package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaRatingService mpaRatingService;

    public Film addFilm(Film film) {
        enrichFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (film == null) {
            throw new IllegalArgumentException("Фильм не может быть null");
        }
        enrichFilm(film);
        return filmStorage.updateFilm(film);
    }

    public Film getById(long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм", id));
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(long userId, long movieId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));

        filmStorage.getById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм", movieId));

        filmStorage.addLike(movieId, userId);

        log.info("Пользователь id={} поставил лайк фильму id={}", userId, movieId);
    }

    public void removeLike(long userId, long movieId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));

        filmStorage.getById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм", movieId));

        filmStorage.removeLike(movieId, userId);

        log.info("Пользователь id={} удалил лайк с фильма id={}", userId, movieId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    private void enrichFilm(Film film) {
        if (film == null) return;

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> validatedGenres = film.getGenres().stream()
                    .filter(genre -> genre != null && genre.getId() != null)
                    .map(Genre::getId)
                    .distinct()
                    .map(genreService::getById)
                    .sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toList());

            film.setGenres(validatedGenres);
        } else {
            film.setGenres(new ArrayList<>());
        }

        if (film.getRating() != null && film.getRating().getId() != null) {
            MpaRating rating = mpaRatingService.getById(film.getRating().getId());
            film.setRating(rating);
        } else {
            film.setRating(mpaRatingService.getById(1L));
        }
    }
}
