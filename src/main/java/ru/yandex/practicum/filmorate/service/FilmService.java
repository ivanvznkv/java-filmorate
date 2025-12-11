package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.LikeOperationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserValidator userValidator;
    private final FilmValidator filmValidator;

    public void addLike(long userId, long movieId) {
        User user = userValidator.getUserOrThrow(userId);
        Film film = filmValidator.getFilmOrThrow(movieId);

        boolean added = film.getLikes().add(userId);
        if (!added) {
            throw new LikeOperationException("Пользователь уже ставил лайк этому фильму.");
        }
        filmStorage.updateFilm(film);
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, movieId);
    }

    public void removeLike(long userId, long movieId) {
        User user = userValidator.getUserOrThrow(userId);
        Film film = filmValidator.getFilmOrThrow(movieId);

        if (!film.getLikes().contains(userId)) {
            throw new LikeOperationException("Пользователь не ставил лайк к данному фильму");
        }

        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь id={} удалил лайк с фильма id={}", userId, movieId);
    }

    public List<Film> getTop10Films(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
