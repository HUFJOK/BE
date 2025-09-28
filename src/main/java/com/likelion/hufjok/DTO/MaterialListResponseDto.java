package com.likelion.hufjok.DTO;

import java.util.List;

// record는 괄호() 안에 필드를 정의하고, 바로 세미콜론(;)으로 끝나거나 중괄호{}를 엽니다.
// ) {} 와 같은 형태는 문법 오류입니다.
public record MaterialListResponseDto(
        PageInfo pageInfo,
        List<MaterialSummaryDto> materials
) {} // <-- 이 부분을 확인하세요.