package com.studyolle.account;

import com.studyolle.domain.Account;
import com.studyolle.setting.form.Notifications;
import com.studyolle.setting.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid  SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyUpdatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .build();
        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디 올래, 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        mailSender.send(mailMessage);
    }

    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))); // 권한 목록을 가져와줌
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
         Account account = accountRepository.findByEmail(emailOrNickname);

        if (account == null) {
           account =  accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }
        return new UserAccount(account);
    }

    public void verify(Account account) {
        account.completeSignup();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {

        Account saveAccount = accountRepository.findByNickname(account.getNickname()); // 변경감지사용
/*        saveAccount.setUrl(profile.getUrl());
        saveAccount.setOccupation(profile.getOccupation());
        saveAccount.setLocation(profile.getLocation());
        saveAccount.setBio(profile.getBio());
        saveAccount.setProfileImage(profile.getProfileImage());*/
        modelMapper.map(profile,saveAccount);
        login(saveAccount);

    }

    public void updatePassword(Account account, String newPassword) {
        Account saveAccount = accountRepository.findByNickname(account.getNickname()); // 변경감지사용
        saveAccount.setPassword(passwordEncoder.encode(newPassword));
        login(saveAccount);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        Account saveAccount = accountRepository.findByNickname(account.getNickname()); // 변경감지사용
        modelMapper.map(notifications, saveAccount);
        login(saveAccount);
    }

    public void updateNickname(Account account, String nickname) {
        Account saveAccount = accountRepository.findByNickname(account.getNickname()); // 변경감지사용
        saveAccount.setNickname(nickname);
        login(saveAccount);
    }

    public void sendLoginLink(Account account) {
        account.generateToken();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디올레 로그인 링크");
        mailMessage.setText("/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        mailSender.send(mailMessage);
    }
}
