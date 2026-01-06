package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(FilmDbStorage.class)
class FilmDbStorageTests {

    @Autowired
    private final FilmDbStorage filmDbStorage;

    @Test
    void shouldAddFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 12, 1));
        film.setDuration(120);
        MpaRating pg13 = new MpaRating();
        pg13.setName("PG-13");
        film.setRating(pg13);

        Film savedFilm = filmDbStorage.addFilm(film);

        assertThat(savedFilm).isNotNull();

        Collection<Film> films = filmDbStorage.getAll();
        assertThat(films)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("name", "Film");
    }

    @Test
    void shouldGetFilmById() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 12, 2));
        film.setDuration(120);
        MpaRating pg13 = new MpaRating();
        pg13.setName("PG-13");
        film.setRating(pg13);

        filmDbStorage.addFilm(film);

        Film savedFilm = filmDbStorage.getAll().iterator().next();
        Long savedId = savedFilm.getId();

        Optional<Film> filmOptional = filmDbStorage.getById(savedId);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(savedId);
                    assertThat(f.getName()).isEqualTo("Film");
                });
    }

    @Test
    void shouldReturnEmptyOptionalWhenFilmNotFound() {
        Optional<Film> filmOptional = filmDbStorage.getById(999L);

        assertThat(filmOptional).isEmpty();
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 12, 1));
        film.setDuration(120);
        MpaRating g = new MpaRating();
        g.setName("G");
        film.setRating(g);

        filmDbStorage.addFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("New film");
        updatedFilm.setDescription("New description");
        updatedFilm.setReleaseDate(LocalDate.of(2025, 12, 2));
        updatedFilm.setDuration(90);
        MpaRating pg = new MpaRating();
        pg.setName("PG");
        updatedFilm.setRating(pg);

        filmDbStorage.updateFilm(updatedFilm);

        Optional<Film> filmOptional = filmDbStorage.getById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "New film")
                                .hasFieldOrPropertyWithValue("duration", 90)
                );
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2025, 12, 1));
        film1.setDuration(90);
        MpaRating g = new MpaRating();
        g.setName("G");
        film1.setRating(g);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2025, 12, 2));
        film2.setDuration(120);
        MpaRating pg = new MpaRating();
        pg.setName("PG");
        film2.setRating(pg);

        filmDbStorage.addFilm(film1);
        filmDbStorage.addFilm(film2);

        Collection<Film> films = filmDbStorage.getAll();

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Film 1", "Film 2");
    }
}
