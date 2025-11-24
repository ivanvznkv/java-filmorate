package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: пустое название. Фильм: {}", film);
            throw new ValidationException("Название не может быть пустым!");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации: описание длиннее 200 символов. Фильм: {}", film);
            throw new ValidationException("Описание не может быть длиннее 200 символов!");
        }

        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации: некорректная длительность. Фильм: {}", film);
            throw new ValidationException("Продолжительность не может быть равна, или меньше 0");
        }

        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(minDate)) {
            log.warn("Ошибка валидации: некорректная дата релиза. Фильм: {}", film);
            throw new ValidationException("Некорректная дата релиза! Фильм должен быть выпущен не раньше 28 декабря 1895 года.");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        log.info("Получен запрос на обновление фильма: {}", updatedFilm);

        if (updatedFilm.getId() == null) {
            log.warn("Ошибка обновления: не указан id. Фильм: {}", updatedFilm);
            throw new ValidationException("Id должен быть указан");
        }

        Film oldFilm = films.get(updatedFilm.getId());
        if (oldFilm == null) {
            log.warn("Ошибка обновления: фильм с id={} не найден", updatedFilm.getId());
            throw new ValidationException("Фильм с указанным Id не найден");
        }

        if (updatedFilm.getName() != null) {
            if (updatedFilm.getName().isBlank()) {
                log.warn("Ошибка валидации: пустое имя при обновлении. Фильм: {}", updatedFilm);
                throw new ValidationException("Название не может быть пустым!");
            }
            oldFilm.setName(updatedFilm.getName());
        }

        if (updatedFilm.getDescription() != null) {
            if (updatedFilm.getDescription().isBlank() ||
                    updatedFilm.getDescription().length() > 200) {
                log.warn("Ошибка валидации: некорректное описание при обновлении. Фильм: {}", updatedFilm);
                throw new ValidationException("Описание не может быть пустым или длиннее 200 символов!");
            }
            oldFilm.setDescription(updatedFilm.getDescription());
        }

        if (updatedFilm.getDuration() != null) {
            if (updatedFilm.getDuration() <= 0) {
                log.warn("Ошибка валидации: некорректная длительность при обновлении. Фильм: {}", updatedFilm);
                throw new ValidationException("Продолжительность должна быть положительной");
            }
            oldFilm.setDuration(updatedFilm.getDuration());
        }

        if (updatedFilm.getReleaseDate() != null) {
            LocalDate minDate = LocalDate.of(1895, 12, 28);
            if (updatedFilm.getReleaseDate().isBefore(minDate)) {
                log.warn("Ошибка валидации: дата релиза раньше 1895-12-28. Фильм: {}", updatedFilm);
                throw new ValidationException("Некорректная дата релиза!");
            }
            oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
        }

        films.put(oldFilm.getId(), oldFilm);
        log.info("Фильм успешно обновлён: {}", oldFilm);
        return oldFilm;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Запрошен список всех фильмов.");
        return films.values();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
