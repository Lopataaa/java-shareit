package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя с email: {}", userDto.getEmail());

        // Проверка на уникальность email
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Попытка создания пользователя с уже существующим email: {}", userDto.getEmail());
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);

        log.info("Пользователь создан с id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя id: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден с id: {}", userId);
                    return new NotFoundException("Пользователь с ID " + userId + " не найден");
                });

        // Обновление имени, если передано
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            log.debug("Обновление имени пользователя id: {}", userId);
            existingUser.setName(userDto.getName());
        }

        // Обновление email, если передано
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            log.debug("Обновление email пользователя id: {}", userId);
            // Проверка, что email не занят другим пользователем
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                log.warn("Попытка обновить email на уже занятый: {} пользователем id: {}",
                        userDto.getEmail(), userId);
                throw new ConflictException("Email " + userDto.getEmail() + " уже используется другим пользователем");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Пользователь id: {} успешно обновлен", userId);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден с id: {}", userId);
                    return new NotFoundException("Пользователь с ID " + userId + " не найден");
                });

        log.debug("Пользователь id: {} успешно получен", userId);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");

        List<User> users = userRepository.findAll();
        log.debug("Найдено {} пользователей", users.size());

        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя id: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("Попытка удаления несуществующего пользователя id: {}", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь id: {} успешно удален", userId);
    }
}