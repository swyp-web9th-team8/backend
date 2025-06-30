package com.swyp.plogging.backend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.swyp.plogging.backend.user.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled
class UserRepositoryImplTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void findProfileByUserId() {
        ProfileResponse result = userRepository.findProfileByUserId(1L);

        assertThat(result).isNull();
    }
}