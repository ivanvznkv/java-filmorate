package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым!")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов!")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана!")
    @ReleaseDateConstraint
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть указана!")
    @Positive(message = "Продолжительность должна быть больше 0")
    private Integer duration;
}
