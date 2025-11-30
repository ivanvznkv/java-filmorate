package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        controller = new UserController();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Set<ConstraintViolation<User>> validate(User user, Class<?> group) {
        return validator.validate(user, group);
    }

    @Test
    void shouldCreateValidUser() {
        User user = validUser();
        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен содержать ошибок валидации");

        User created = controller.addUser(user);
        assertNotNull(created.getId());
    }

    @Test
    void shouldFailIfEmailMissing() {
        User user = validUser();
        user.setEmail(null);

        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Имэйл не может быть пустым")));
    }

    @Test
    void shouldFailIfEmailNoAtSymbol() {
        User user = validUser();
        user.setEmail("wrongemail");

        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Имэйл должен содержать символ @")));
    }

    @Test
    void shouldFailIfLoginEmpty() {
        User user = validUser();
        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Логин не может быть пустым")));
    }

    @Test
    void shouldFailIfLoginContainsSpaces() {
        User user = validUser();
        user.setLogin("iv an");

        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Логин не должен содержать пробелы")));
    }

    @Test
    void shouldSetLoginAsNameIfNameEmpty() {
        User user = validUser();
        user.setName("");

        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertTrue(violations.isEmpty(), "Проверяем, что аннотации не запрещают пустое имя");

        User created = controller.addUser(user);
        assertEquals("ivan", created.getName());
    }

    @Test
    void shouldFailIfBirthdayInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Дата рождения не может быть в будущем")));
    }

    @Test
    void shouldFailOnEmptyUser() {
        User user = new User();
        Set<ConstraintViolation<User>> violations = validate(user, ValidationGroups.OnCreate.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldUpdateValidUser() {
        User user = validUser();
        user.setId(1L);
        controller.addUser(user);

        User updated = new User();
        updated.setId(user.getId());
        updated.setName("Петя");
        updated.setEmail("petya@test.com");
        updated.setLogin("petya");
        updated.setBirthday(LocalDate.of(1995, 5, 5));

        Set<ConstraintViolation<User>> violations = validate(updated, ValidationGroups.OnUpdate.class);
        assertTrue(violations.isEmpty());

        User result = controller.updateUser(updated);
        assertEquals("Петя", result.getName());
        assertEquals("petya@test.com", result.getEmail());
    }

    @Test
    void shouldFailUpdateIfIdMissing() {
        User updated = validUser();
        Exception ex = assertThrows(Exception.class, () -> controller.updateUser(updated));
        assertTrue(ex.getMessage().contains("не найден"));
    }
}
