package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exception.LikeOperationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
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
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        Film film = filmStorage.getById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм", movieId));

        boolean added = film.getLikes().add(userId);
        if (!added) {
            throw new LikeOperationException("Пользователь уже ставил лайк этому фильму.");
        }
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, movieId);
    }

    public void removeLike(long userId, long movieId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь", userId));
        Film film = filmStorage.getById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Фильм", movieId));

        if (!film.getLikes().contains(userId)) {
            throw new LikeOperationException("Пользователь не добавлял лайк к данному фильму");
        }

        film.getLikes().remove(userId);
        log.info("Пользователь id={} удалил лайк с фильма id={}", userId, movieId);
    }

    public List<Film> getTop10Films(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Сount должен быть больше нуля");
        }

        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
