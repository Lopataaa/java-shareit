package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;

public interface CommentService {
    CommentDto createComment(Long itemId, Long userId, CommentDto commentDto);
}
