package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingStorage mpaRatingStorage;

    public Collection<MpaRating> getAll() {
        return mpaRatingStorage.getAll();
    }

    public MpaRating getById(Long id) {
        return mpaRatingStorage.getById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("MPA рейтинг", id));
    }
}
