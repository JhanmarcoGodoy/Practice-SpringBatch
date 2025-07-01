package com.mysql.demo.Service;

import com.mysql.demo.Model.User;
import com.mysql.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }


    @Transactional
    public User createUser(User user) {
        if (user.getId() != null) {
            // Lanzar una RuntimeException hará que la transacción haga rollback automáticamente.
            throw new IllegalArgumentException("El ID debe ser nulo para crear un nuevo usuario.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("El email '" + user.getEmail() + "' ya está en uso.");
        }
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Dentro de una transacción, la entidad 'existingUser' está en estado "managed".
                    // Los cambios se rastrean y se guardan al final del método (commit).
                    existingUser.setFirstName(userDetails.getFirstName());
                    existingUser.setLastName(userDetails.getLastName());
                    existingUser.setEmail(userDetails.getEmail());
                    // Aunque .save() se llama explícitamente, en muchos casos con JPA,
                    // los cambios a una entidad "managed" se persistirían automáticamente al hacer commit.
                    // Llamarlo explícitamente es seguro y claro.
                    return userRepository.save(existingUser);
                });
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}