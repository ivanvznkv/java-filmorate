package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;

/**
 * User.
 */

@Data
@EqualsAndHashCode(of = "id")
public class User {
    @Null(groups = ValidationGroups.OnCreate.class)
    @NotNull(groups = ValidationGroups.OnUpdate.class)
    private Long id;
    private String name;

    @NotBlank(message = "Имэйл не может быть пустым", groups = ValidationGroups.OnCreate.class)
    @Email(message = "Имэйл должен содержать символ @", groups = ValidationGroups.OnCreate.class)
    private String email;

    @NotBlank(message = "Логин не может быть пустым", groups = ValidationGroups.OnCreate.class)
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы", groups = ValidationGroups.OnCreate.class)
    private String login;

    @NotNull(message = "Дата рождения не может быть пустой", groups = ValidationGroups.OnCreate.class)
    @PastOrPresent(message = "Дата рождения не может быть в будущем", groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdate.class})
    private LocalDate birthday;
}
