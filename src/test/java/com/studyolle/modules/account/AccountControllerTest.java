package com.studyolle.modules.account;

import com.studyolle.infra.MockMvcTest;
import com.studyolle.infra.mail.EmailMessage;
import com.studyolle.infra.mail.EmailService;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.account.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("회원가입 폼 테스트")
    public void signUpForm() throws Exception {

        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리: 입력값 오류")
    public void signUpSubmitWithError() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "nickname")
                        .param("email", "test@email")
                        .param("password", "1234")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 : 입력값 정상")
    public void signUpSubmit() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "nickname")
                        .param("email", "test@gmail.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("nickname"));

        Account account = accountRepository.findByEmail("test@gmail.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");
        assertNotNull(account.getEmailCheckToken());
        then(emailService).should().sendEmail(any(EmailMessage.class));

    }


    @Test
    @DisplayName("인증 메일 확인 : 잘못된 링크")
    public void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "test123123")
                        .param("email", "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/checked-email"))
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("인증 메일 확인 : 유효한 링크")
    @Transactional
    public void checkEmailToken() throws Exception {
        Account account =Account.builder()
                .email("test@email.com")
                .password("123456789")
                .nickname("test")
                .build();

        Account newAccount = accountRepository.save(account);
        newAccount.generateToken();

        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname", "numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("test"));
    }
}