package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    @Null(groups = ValidationGroups.OnCreate.class)
    @NotNull(groups = ValidationGroups.OnUpdate.class)
    private Long id;

    @NotBlank(message = "Название не может быть пустым!", groups = ValidationGroups.OnCreate.class)
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов!", groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    private String description;

    @NotNull(message = "Дата релиза должна быть указана!", groups = ValidationGroups.OnCreate.class)
    @ReleaseDateConstraint(groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть указана!", groups = ValidationGroups.OnCreate.class)
    @Positive(message = "Продолжительность должна быть больше 0", groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    private Integer duration;
}
