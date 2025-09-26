package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todos")
@Tag(name = "Todos", description = "투두 관리 API")
public class TodoController {

    private final TodoService service;

    // 투두 생성
    @Operation(summary = "투두 생성")
    @PostMapping
    public ResponseEntity<TodoCreateResDto> create(@Valid @RequestBody TodoCreateReqDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    // 투두 목록 조회
    // completed 파라미터가 없으면 "전체 조회", 있으면 true/false 필터 조회
    @Operation(summary = "투두 목록 조회", description = "completed 파라미터가 없으면 전체 조회, 있으면 해당 상태로 필터링")
    @GetMapping
    public List<TodoGetResDto> list(@RequestParam(required = false) Boolean completed) {
        return service.list(completed); // null -> 전체, true/false -> 필터
    }

    // 투두 단건 조회
    @Operation(summary = "투두 단건 조회")
    @GetMapping("/{id}")
    public TodoGetResDto get(@PathVariable Long id) {
        return service.get(id);
    }

    // 투두 전체 수정 (PUT: 모든 수정 가능 필드 반드시 포함)
    @Operation(summary = "투두 전체 수정")
    @PutMapping("/{id}")
    public TodoUpdateResDto update(@PathVariable Long id, @Valid @RequestBody TodoUpdateReqDto req) {
        return service.update(id, req);
    }

    // 투두 완료여부 변경 (부분 수정)
    @Operation(summary = "투두 완료여부 변경")
    @PatchMapping("/{id}/done")
    public TodoDoneResDto changeDone(@PathVariable Long id, @Valid @RequestBody TodoDoneReqDto req) {
        return service.changeDone(id, req);
    }

    // 투두 삭제
    @Operation(summary = "투두 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
