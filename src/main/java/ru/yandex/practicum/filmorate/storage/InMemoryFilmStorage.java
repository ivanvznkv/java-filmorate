package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exception.LikeOperationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private long nextId = 1;
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
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

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new EntityNotFoundException("Фильм", filmId);
        }

        if (!film.getLikes().add(userId)) {
            throw new LikeOperationException("Пользователь уже ставил лайк этому фильму.");
        }

        log.info("Добавлен лайк: пользователь {} → фильм {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new EntityNotFoundException("Фильм", filmId);
        }

        if (!film.getLikes().remove(userId)) {
            throw new LikeOperationException("Пользователь не добавлял лайк к данному фильму");
        }

        log.info("Удалён лайк: пользователь {} → фильм {}", userId, filmId);
    }

    private Long getNextId() {
        return nextId++;
    }
}