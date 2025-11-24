package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {

    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Description");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        return film;
    }

    @Test
    void shouldAddValidFilm() {
        Film film = validFilm();
        Film saved = controller.addFilm(film);

        assertNotNull(saved.getId());
        assertEquals("Test Film", saved.getName());
    }

    @Test
    void shouldFailIfNameEmpty() {
        Film film = validFilm();
        film.setName("");

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.addFilm(film)
        );

        assertEquals("Название не может быть пустым!", ex.getMessage());
    }

    @Test
    void shouldFailIfDescriptionTooLong() {
        Film film = validFilm();
        film.setDescription("A".repeat(201));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.addFilm(film)
        );

        assertEquals("Описание не может быть длиннее 200 символов!", ex.getMessage());
    }

    @Test
    void shouldFailIfDurationNegative() {
        Film film = validFilm();
        film.setDuration(-10);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.addFilm(film)
        );

        assertEquals("Продолжительность не может быть равна, или меньше 0", ex.getMessage());
    }

    @Test
    void shouldFailIfDurationZero() {
        Film film = validFilm();
        film.setDuration(0);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.addFilm(film)
        );

        assertEquals("Продолжительность не может быть равна, или меньше 0", ex.getMessage());
    }

    @Test
    void shouldFailIfReleaseDateNull() {
        Film film = validFilm();
        film.setReleaseDate(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.addFilm(film)
        );

        assertTrue(ex.getMessage().contains("Некорректная дата релиза"));
    }

    @Test
    void shouldFailIfReleaseDateTooEarly() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.addFilm(film)
        );

        assertTrue(ex.getMessage().contains("Некорректная дата релиза"));
    }

    @Test
    void shouldFailOnEmptyObject() {
        Film film = new Film();

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldFailUpdateIfIdMissing() {
        Film film = validFilm();
        controller.addFilm(film);

        Film update = new Film();
        update.setName("New");

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(update)
        );

        assertEquals("Id должен быть указан", ex.getMessage());
    }

    @Test
    void shouldFailUpdateNonExistingFilm() {
        Film update = validFilm();
        update.setId(999L);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(update)
        );

        assertEquals("Фильм с указанным Id не найден", ex.getMessage());
    }

    @Test
    void shouldUpdateValidFilm() {
        Film film = controller.addFilm(validFilm());

        Film update = new Film();
        update.setId(film.getId());
        update.setName("Updated");
        update.setDuration(150);

        Film updated = controller.updateFilm(update);

        assertEquals("Updated", updated.getName());
        assertEquals(150, updated.getDuration());
    }

    @Test
    void shouldFailUpdateIfNewNameBlank() {
        Film film = controller.addFilm(validFilm());

        Film update = new Film();
        update.setId(film.getId());
        update.setName("");

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(update)
        );

        assertEquals("Название не может быть пустым!", ex.getMessage());
    }

    @Test
    void shouldFailUpdateIfDescriptionInvalid() {
        Film film = controller.addFilm(validFilm());

        Film update = new Film();
        update.setId(film.getId());
        update.setDescription(" ".repeat(5));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(update)
        );

        assertTrue(ex.getMessage().contains("Описание"));
    }

    @Test
    void shouldFailUpdateIfDurationInvalid() {
        Film film = controller.addFilm(validFilm());

        Film update = new Film();
        update.setId(film.getId());
        update.setDuration(0);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(update)
        );

        assertEquals("Продолжительность должна быть положительной", ex.getMessage());
    }

    @Test
    void shouldFailUpdateIfReleaseDateInvalid() {
        Film film = controller.addFilm(validFilm());

        Film update = new Film();
        update.setId(film.getId());
        update.setReleaseDate(LocalDate.of(1500, 1, 1));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(update)
        );

        assertEquals("Некорректная дата релиза!", ex.getMessage());
    }
}
