package com.studyolle.modules.main;

import com.studyolle.infra.AbstractContainerBaseTest;
import com.studyolle.infra.MockMvcTest;
import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.account.AccountService;
import com.studyolle.modules.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class MainControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;


    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("test123");
        signUpForm.setEmail("test123@gmail.com");
        signUpForm.setPassword("pass1234");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void AfterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("이메일 로그인 : 성공")
    public void login_with_email() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "test123@gmail.com")
                        .param("password", "pass1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test123"));
    }

    @Test
    @DisplayName("닉네임 로그인 : 성공")
    public void login_with_nickname() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "test123")
                        .param("password", "pass1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test123"));

    }


    @Test
    @DisplayName("로그인 실패")
    public void login_fail() throws Exception {

        mockMvc.perform(post("/login")
                        .param("username", "test123")
                        .param("password","test123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그아웃: 성공")
    void logout() throws Exception {
        mockMvc.perform(post("/logout") // (1)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")) // (2)
                .andExpect(unauthenticated()); // (3)
    }
}