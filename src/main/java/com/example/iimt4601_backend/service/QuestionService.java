package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.entity.Question;
import com.example.iimt4601_backend.entity.User;
import com.example.iimt4601_backend.repository.ProductRepository;
import com.example.iimt4601_backend.repository.QuestionRepository;
import com.example.iimt4601_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    public QuestionListResponseDto getProductQuestions(Long productId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Question> questionPage = questionRepository.findByProductId(productId, pageable);

        List<QuestionDto> questionDtos = questionPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new QuestionListResponseDto(
                (int) questionPage.getTotalElements(),
                page,
                limit,
                questionDtos
        );
    }

    public QuestionListResponseDto getUserQuestions(Long userId, int page, int limit) {
        // 사용자 존재 여부 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Question> questionPage = questionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<QuestionDto> questionDtos = questionPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new QuestionListResponseDto(
                (int) questionPage.getTotalElements(),
                page,
                limit,
                questionDtos
        );
    }

    public QuestionDto addQuestion(Long productId, QuestionRequestDto requestDto, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setProduct(product);
        question.setUser(user); // User 엔티티 설정
        question.setTitle(requestDto.getTitle());
        question.setContent(requestDto.getContent());
        question.setIsPrivate(requestDto.getIsPrivate());

        questionRepository.save(question);

        return convertToDto(question);
    }

    private QuestionDto convertToDto(Question question) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        UserMinimalDto userDto = new UserMinimalDto();
        userDto.setId(question.getUser().getId());
        userDto.setUserName(question.getUser().getUserName());
        userDto.setEmail(question.getUser().getEmail());
        userDto.setPhoneNumber(question.getUser().getPhoneNumber());

        return new QuestionDto(
                question.getId(),
                userDto,
                question.getTitle(),
                question.getContent(),
                question.getIsPrivate(),
                question.getCreatedAt().format(formatter),
                question.getAnswer(),
                question.getAnsweredAt() != null ? question.getAnsweredAt().format(formatter) : null
        );
    }
}
