package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException(
                    "User with email " + userDto.getEmail() + " already exists");
        }
        User user = UserMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = getUserOrThrow(id);

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (userRepository.existsByEmail(userDto.getEmail())
                    && !userDto.getEmail().equals(existingUser.getEmail())) {
                throw new ConflictException(
                        "User with email " + userDto.getEmail() + " already exists");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getById(Long id) {
        User user = getUserOrThrow(id);
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return UserMapper.toDto(userRepository.findAll());
    }

    @Override
    public void delete(Long id) {
        getUserOrThrow(id);
        userRepository.deleteById(id);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }
}
