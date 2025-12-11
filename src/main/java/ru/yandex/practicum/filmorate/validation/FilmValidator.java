package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilmValidator {
    private final FilmStorage filmStorage;

    public Film getFilmOrThrow(long id) {
        log.info("Запрошен фильм с id={}", id);
        Film film = filmStorage.getById(id);
        if (film == null) {
            log.warn("Фильм с id={} не найден", id);
            throw new EntityNotFoundException("Фильм", id);
        }
        return film;
    }
}
