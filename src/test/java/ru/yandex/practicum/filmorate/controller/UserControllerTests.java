package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController controller;

    @BeforeEach
    void setup() {
        controller = new UserController();
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = controller.addUser(user);

        assertNotNull(created.getId());
        assertEquals("ivan", created.getLogin());
    }

    @Test
    void shouldFailIfEmailMissing() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("ivan");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class,
                () -> controller.addUser(user));
    }

    @Test
    void shouldFailIfEmailNoAtSymbol() {
        User user = new User();
        user.setEmail("invalid.email");
        user.setLogin("ivan");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class,
                () -> controller.addUser(user));
    }

    @Test
    void shouldFailIfLoginEmpty() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class,
                () -> controller.addUser(user));
    }

    @Test
    void shouldFailIfLoginContainsSpaces() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("iv an");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class,
                () -> controller.addUser(user));
    }

    @Test
    void shouldSetLoginAsNameIfNameEmpty() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = controller.addUser(user);

        assertEquals("ivan", created.getName());
    }

    @Test
    void shouldFailIfBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class,
                () -> controller.addUser(user));
    }

    @Test
    void shouldFailOnEmptyJson() {
        User user = new User();

        assertThrows(ValidationException.class,
                () -> controller.addUser(user));
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = controller.addUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setEmail("new@test.com");

        User updated = controller.updateUser(update);

        assertEquals("new@test.com", updated.getEmail());
    }

    @Test
    void shouldFailUpdateIfNoId() {
        User update = new User();
        update.setEmail("test@test.com");

        assertThrows(ValidationException.class,
                () -> controller.updateUser(update));
    }

    @Test
    void shouldFailUpdateIfUserNotFound() {
        User update = new User();
        update.setId(99L);
        update.setEmail("test@test.com");

        assertThrows(ValidationException.class,
                () -> controller.updateUser(update));
    }

    @Test
    void shouldFailUpdateIfInvalidEmail() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("ivan");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        controller.addUser(user);

        User update = new User();
        update.setId(1L);
        update.setEmail("wrong");

        assertThrows(ValidationException.class,
                () -> controller.updateUser(update));
    }
}
