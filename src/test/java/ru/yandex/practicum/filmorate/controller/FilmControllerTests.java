package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.LikeOperationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {
    private FilmController controller;
    private Validator validator;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        FilmValidator filmValidator = new FilmValidator(filmStorage);
        UserValidator userValidator = new UserValidator(userStorage);
        FilmService filmService = new FilmService(filmStorage, userValidator, filmValidator);
        controller = new FilmController(filmStorage, filmService);
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

    private User validUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Set<ConstraintViolation<Film>> validate(Film film, Class<?> group) {
        return validator.validate(film, group);
    }

    // --- STORAGE тесты
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

    // --- SERVICE тесты
    @Test
    void shouldAddAndRemoveLike() {
        User user = userStorage.addUser(validUser());
        Film film = controller.addFilm(validFilm());

        controller.addLike(user.getId(), film.getId());
        Film updated = filmStorage.getById(film.getId());
        assertTrue(updated.getLikes().contains(user.getId()));

        controller.removeLike(user.getId(), film.getId());
        updated = filmStorage.getById(film.getId());
        assertFalse(updated.getLikes().contains(user.getId()));
    }

    @Test
    void removeLikeShouldThrowIfNoLike() {
        User user = userStorage.addUser(validUser());
        Film film = controller.addFilm(validFilm());

        LikeOperationException ex = assertThrows(LikeOperationException.class,
                () -> controller.removeLike(user.getId(), film.getId()));
        assertEquals("Пользователь не добавлял лайк к данному фильму", ex.getMessage());
    }

    @Test
    void getPopularShouldReturnFilmsInOrder() {
        User user1 = userStorage.addUser(validUser());
        User user2 = userStorage.addUser(validUser());
        Film film1 = controller.addFilm(validFilm());
        Film film2 = controller.addFilm(validFilm());

        controller.addLike(film2.getId(), user1.getId());
        controller.addLike(film2.getId(), user2.getId());
        controller.addLike(film1.getId(), user1.getId());

        List<Film> topFilms = controller.getPopular(2);
        assertEquals(film2.getId(), topFilms.get(0).getId());
        assertEquals(film1.getId(), topFilms.get(1).getId());
    }
}
