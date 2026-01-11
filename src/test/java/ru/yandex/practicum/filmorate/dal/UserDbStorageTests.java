package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class UserDbStorageTests {

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void shouldAddUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        userDbStorage.addUser(user);

        Collection<User> users = userDbStorage.getAll();

        assertThat(users)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("email", "test@mail.ru")
                .hasFieldOrPropertyWithValue("login", "testlogin");
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1995, 5, 5));

        userDbStorage.addUser(user);

        User savedUser = userDbStorage.getAll().iterator().next();
        Long savedId = savedUser.getId();

        Optional<User> userOptional = userDbStorage.getById(savedId);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(savedId);
                    assertThat(u.getEmail()).isEqualTo("user@mail.ru");
                    assertThat(u.getLogin()).isEqualTo("userlogin");
                });
    }

    @Test
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        Optional<User> userOptional = userDbStorage.getById(999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("old@mail.ru");
        user.setLogin("oldlogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1980, 1, 1));

        userDbStorage.addUser(user);

        User savedUser = userDbStorage.getAll().iterator().next();

        User updatedUser = new User();
        updatedUser.setId(savedUser.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");
        updatedUser.setBirthday(LocalDate.of(1990, 2, 2));

        userDbStorage.updateUser(updatedUser);

        Optional<User> userOptional = userDbStorage.getById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getEmail()).isEqualTo("new@mail.ru");
                    assertThat(u.getLogin()).isEqualTo("newlogin");
                    assertThat(u.getName()).isEqualTo("New Name");
                });
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User();
        user1.setEmail("first@mail.ru");
        user1.setLogin("first");
        user1.setName("First");
        user1.setBirthday(LocalDate.of(1991, 1, 1));

        User user2 = new User();
        user2.setEmail("second@mail.ru");
        user2.setLogin("second");
        user2.setName("Second");
        user2.setBirthday(LocalDate.of(1992, 2, 2));

        userDbStorage.addUser(user1);
        userDbStorage.addUser(user2);

        Collection<User> users = userDbStorage.getAll();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("first", "second");
    }
}
