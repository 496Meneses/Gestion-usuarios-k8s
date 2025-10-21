package com.sabana.service;

import com.sabana.exception.UserNotFoundException;
import com.sabana.model.User;
import com.sabana.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public User findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User create(User user) {
        user.setId(null);
        return repo.save(user);
    }

    public User update(Long id, User payload) {
        User existing = repo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        existing.setName(payload.getName());
        existing.setEmail(payload.getEmail());
        return repo.save(existing);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new UserNotFoundException(id);
        repo.deleteById(id);
    }
}
