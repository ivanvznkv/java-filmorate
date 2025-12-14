package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Validated
public class FilmController {
    private final FilmService filmService;

    // --- STORAGE операции
    @PostMapping
    public Film addFilm(@Validated(ValidationGroups.OnCreate.class) @RequestBody Film film) {
        log.debug("POST /films {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Validated(ValidationGroups.OnUpdate.class) @RequestBody Film updatedFilm) {
        log.debug("PUT /films {}", updatedFilm);
        return filmService.updateFilm(updatedFilm);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        log.debug("GET /films/{}", id);
        return filmService.getById(id);
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.debug("GET /films");
        return filmService.getAll();
    }

    // --- SERVICE операции
    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable Long filmId, @PathVariable Long userId) {
        log.debug("PUT /films/{}/like/{}", filmId, userId);
        filmService.addLike(userId, filmId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable Long filmId, @PathVariable Long userId) {
        log.debug("DELETE /films/{}/like/{}", filmId, userId);
        filmService.removeLike(userId, filmId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") @Positive int count) {
        log.debug("GET /films/popular?count={}", count);
        return filmService.getPopularFilms(count);
    }
}
