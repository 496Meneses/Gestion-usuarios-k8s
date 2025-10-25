package com.sabana.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sabana.exception.UserNotFoundException;
import com.sabana.model.User;
import com.sabana.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

  private UserRepository repo;
  private UserService service;

  @BeforeEach
  void setUp() {
    repo = mock(UserRepository.class);
    service = new UserService(repo);
  }

  @Test
  void shouldFindAllUsers() {
    List<User> users = List.of(
        User.builder()
            .id(1L)
            .name("Alice")
            .email("alice@example.com")
            .createdAt(Instant.now())
            .build()
    );
    when(repo.findAll()).thenReturn(users);

    List<User> result = service.findAll();

    assertEquals(1, result.size());
    assertEquals(users.get(0), result.get(0));
  }

  @Test
  void shouldFindUserById() {
    User user = User.builder()
        .id(1L)
        .name("Bob")
        .email("bob@example.com")
        .createdAt(Instant.now())
        .build();
    when(repo.findById(1L)).thenReturn(Optional.of(user));

    User result = service.findById(1L);

    assertEquals(user, result);
  }

  @Test
  void shouldThrowExceptionWhenUserNotFoundById() {
    when(repo.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.findById(1L));
  }

  @Test
  void shouldUpdateExistingUser() {
    User existing = User.builder()
        .id(3L)
        .name("David")
        .email("david@old.com")
        .createdAt(Instant.now())
        .build();

    User payload = User.builder()
        .name("David Updated")
        .email("david@new.com")
        .build();

    when(repo.findById(3L)).thenReturn(Optional.of(existing));
    when(repo.save(any(User.class))).thenReturn(existing);

    User result = service.update(3L, payload);

    assertEquals("David Updated", result.getName());
    assertEquals("david@new.com", result.getEmail());
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistentUser() {
    User payload = User.builder()
        .name("Eve")
        .email("eve@example.com")
        .build();

    when(repo.findById(4L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.update(4L, payload));
  }

  @Test
  void shouldDeleteUserById() {
    when(repo.existsById(5L)).thenReturn(true);

    service.delete(5L);

    verify(repo).deleteById(5L);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistentUser() {
    when(repo.existsById(6L)).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> service.delete(6L));
  }
}
