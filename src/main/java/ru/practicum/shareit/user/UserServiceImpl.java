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
    private final UserMapper userMapper;

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("User with email " + userDto.getEmail() + " already exists");
        }
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = getUserOrThrow(id);

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (userRepository.existsByEmail(userDto.getEmail()) &&
                    !userDto.getEmail().equals(existingUser.getEmail())) {
                throw new ConflictException("User with email " + userDto.getEmail() + " already exists");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getById(Long id) {
        User user = getUserOrThrow(id);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userMapper.toDto(userRepository.findAll());
    }

    @Override
    public void delete(Long id) {
        getUserOrThrow(id);
        userRepository.deleteById(id);
    }
}

