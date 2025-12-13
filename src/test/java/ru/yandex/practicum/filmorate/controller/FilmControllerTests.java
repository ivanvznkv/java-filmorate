package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {

    private FilmController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);
        controller = new FilmController(filmService);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        return film;
    }

    private Set<ConstraintViolation<Film>> validate(Film film, Class<?> group) {
        return validator.validate(film, group);
    }

    @Test
    void shouldAddValidFilm() {
        Film film = validFilm();
        Set<ConstraintViolation<Film>> violations = validate(film, ValidationGroups.OnCreate.class);
        assertTrue(violations.isEmpty(), "Фильм с валидными данными не должен содержать ошибок валидации");

        Film saved = controller.addFilm(film);
        assertNotNull(saved.getId());
    }

    @Test
    void shouldFailIfNameEmpty() {
        Film film = validFilm();
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validate(film, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Название не может быть пустым!")));
    }

    @Test
    void shouldFailIfDescriptionTooLong() {
        Film film = validFilm();
        film.setDescription("A".repeat(201));

        Set<ConstraintViolation<Film>> violations = validate(film, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Описание не может быть длиннее 200 символов")));
    }

    @Test
    void shouldFailIfDurationInvalid() {
        Film film = validFilm();
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validate(film, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Продолжительность должна быть больше 0")));
    }

    @Test
    void shouldFailIfReleaseDateInvalid() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        Set<ConstraintViolation<Film>> violations = validate(film, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Некорректная дата релиза")));
    }

    @Test
    void shouldFailIfReleaseDateNull() {
        Film film = validFilm();
        film.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validate(film, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Дата релиза должна быть указана")));
    }
}
