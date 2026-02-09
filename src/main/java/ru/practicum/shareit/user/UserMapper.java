package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public BookingResponseDto.BookerDto toBookerDto(User user) {
        if (user == null) {
            return null;
        }
        return BookingResponseDto.BookerDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}