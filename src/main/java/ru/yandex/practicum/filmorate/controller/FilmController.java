package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@Validated(ValidationGroups.OnCreate.class) @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Validated(ValidationGroups.OnUpdate.class) @RequestBody Film updatedFilm) {
        log.info("Получен запрос на обновление фильма: {}", updatedFilm);

        if (!films.containsKey(updatedFilm.getId())) {
            log.warn("Ошибка обновления: фильм с id={} не найден", updatedFilm.getId());
            throw new EntityNotFoundException("Фильм", updatedFilm.getId());
        }

        Film oldFilm = films.get(updatedFilm.getId());

        if (updatedFilm.getName() != null) {
            oldFilm.setName(updatedFilm.getName());
        }
        if (updatedFilm.getDescription() != null) {
            oldFilm.setDescription(updatedFilm.getDescription());
        }
        if (updatedFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
        }
        if (updatedFilm.getDuration() != null) {
            oldFilm.setDuration(updatedFilm.getDuration());
        }

        log.info("Фильм успешно обновлён: {}", updatedFilm);
        return updatedFilm;
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
