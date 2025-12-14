package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */
@Data
public class User {
    private String name;
    @Null(groups = ValidationGroups.OnCreate.class)
    @NotNull(groups = ValidationGroups.OnUpdate.class)
    private Long id;

    @NotBlank(message = "Имэйл не может быть пустым", groups = ValidationGroups.OnCreate.class)
    @Email(message = "Имэйл должен содержать символ @", groups = ValidationGroups.OnCreate.class)
    private String email;

    @NotBlank(message = "Логин не может быть пустым", groups = ValidationGroups.OnCreate.class)
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы", groups = ValidationGroups.OnCreate.class)
    private String login;

    @NotNull(message = "Дата рождения не может быть пустой", groups = ValidationGroups.OnCreate.class)
    @PastOrPresent(message = "Дата рождения не может быть в будущем", groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
}
