package com.likelion.todolist.service;

import com.likelion.todolist.DTO.*;
import com.likelion.todolist.domain.Todo;
import com.likelion.todolist.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository repo;

    public TodoCreateResDto create(TodoCreateReqDto req) {
        Todo todo = Todo.builder()
                .title(req.title())
                .completed(false)
                .build();
        Todo saved = repo.save(todo);
        return new TodoCreateResDto(
                saved.getId(), saved.getTitle(), saved.isCompleted(),
                saved.getCreatedAt(), saved.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<TodoGetResDto> list(Boolean completed) {
        var list = (completed == null) ? repo.findAll() : repo.findByCompleted(completed);
        return list.stream()
                .map(t -> new TodoGetResDto(
                        t.getId(), t.getTitle(), t.isCompleted(),
                        t.getCreatedAt(), t.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TodoGetResDto get(Long id) {
        Todo t = repo.findById(id).orElseThrow(() -> new NotFoundException("Todo", id));
        return new TodoGetResDto(
                t.getId(), t.getTitle(), t.isCompleted(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }

    public TodoUpdateResDto update(Long id, TodoUpdateReqDto req) {
        Todo t = repo.findById(id).orElseThrow(() -> new NotFoundException("Todo", id));
        t.setTitle(req.title());
        t.setCompleted(Boolean.TRUE.equals(req.completed()));
        Todo saved = repo.save(t);
        return new TodoUpdateResDto(
                saved.getId(), saved.getTitle(), saved.isCompleted(),
                saved.getCreatedAt(), saved.getUpdatedAt()
        );
    }

    public TodoDoneResDto changeDone(Long id, TodoDoneReqDto req) {
        Todo t = repo.findById(id).orElseThrow(() -> new NotFoundException("Todo", id));
        t.setCompleted(Boolean.TRUE.equals(req.completed()));
        Todo saved = repo.save(t);
        return new TodoDoneResDto(
                saved.getId(), saved.getTitle(), saved.isCompleted(),
                saved.getCreatedAt(), saved.getUpdatedAt()
        );
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Todo", id);
        repo.deleteById(id);
    }
}

