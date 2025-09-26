package com.likelion.todolist.DTO;

public record TodoGetReqDto(
        Boolean completed // null이면 전체
) {}
