package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */
@Data
@EqualsAndHashCode(of = "id")
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
    private List<Genre> genres = new ArrayList<>();

    @JsonProperty("mpa")
    private MpaRating rating;
    private Set<Long> likes = new HashSet<>();
}
