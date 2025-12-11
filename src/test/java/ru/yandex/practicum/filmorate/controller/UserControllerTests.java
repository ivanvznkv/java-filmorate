package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;
import ru.yandex.practicum.filmorate.validation.ValidationGroups;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController controller;
    private Validator validator;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        UserValidator userValidator = new UserValidator(userStorage);
        UserService userService = new UserService(userStorage, userValidator);
        controller = new UserController(userStorage, userService);
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

    // --- STORAGE тесты
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

    // --- SERVICE тесты
    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = controller.addUser(validUser());
        User user2 = controller.addUser(validUser());

        controller.addFriend(user1.getId(), user2.getId());
        User updated1 = userStorage.getById(user1.getId());
        User updated2 = userStorage.getById(user2.getId());
        assertTrue(updated1.getFriends().contains(user2.getId()));
        assertTrue(updated2.getFriends().contains(user1.getId()));

        controller.removeFriend(user1.getId(), user2.getId());
        updated1 = userStorage.getById(user1.getId());
        updated2 = userStorage.getById(user2.getId());
        assertFalse(updated1.getFriends().contains(user2.getId()));
        assertFalse(updated2.getFriends().contains(user1.getId()));
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = controller.addUser(validUser());
        User user2 = controller.addUser(validUser());
        User user3 = controller.addUser(validUser());

        controller.addFriend(user1.getId(), user3.getId());
        controller.addFriend(user2.getId(), user3.getId());

        List<User> commonFriends = controller.getCommonFriends(user1.getId(), user2.getId()).stream().toList();
        assertEquals(1, commonFriends.size());
        assertEquals(user3.getId(), commonFriends.get(0).getId());
    }
}
