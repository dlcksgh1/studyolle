package com.studyolle.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.infra.MockMvcTest;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;
import com.studyolle.modules.account.form.TagForm;
import com.studyolle.modules.account.form.ZoneForm;
import com.studyolle.modules.tag.TagRepository;
import com.studyolle.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("test123")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONE_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @Test
    @DisplayName("계정의 지역 정보 추가")
    @WithAccount("test123")
    void addZone() throws Exception {
        zoneRepository.save(testZone);
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(SettingsController.SETTINGS_ZONE_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        Account account = accountRepository.findByNickname("test123");
        assertTrue(account.getZones().contains(testZone));
    }

    @Test
    @DisplayName("계정의 지역 정보 삭제")
    @WithAccount("test123")
    void removeZone() throws Exception {
        Account test123 = accountRepository.findByNickname("test123");
        zoneRepository.save(testZone);
        accountService.addZone(test123, testZone);
        assertTrue(test123.getZones().contains(testZone));
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(post(SettingsController.SETTINGS_ZONE_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertFalse(test123.getZones().contains(testZone));
    }


    @Test
    @DisplayName("프로필 수정 : 입력값 정상")
    @WithAccount("test123")
    void updateProfile() throws Exception {
        String bio = "한 줄 소개";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL)) // (3)
                .andExpect(flash().attributeExists("message")); // (4)
        Account account = accountRepository.findByNickname("test123"); // (5)
        assertEquals(bio, account.getBio());
    }

    @Test
    @DisplayName("프로필 수정: 입력값 에러")
    @WithAccount("test123")
    void updateProfileWithError() throws Exception {
        String bio = "35자 이상 test123test123test123test123test123test123test123test123test123test123test123";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
        Account account = accountRepository.findByNickname("test123");
        assertNull(account.getBio());
    }

    @Test
    @DisplayName("프로필 조회")
    @WithAccount("test123")
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    @DisplayName("패스워드 수정 폼")
    @WithAccount("test123")
    public void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    @DisplayName("패스워드 수정:입력값 정상")
    @WithAccount("teat123")
    public void updatePassword() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword", "123456789")
                        .param("newPasswordConfirm", "123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname("teat123");
        assertTrue(passwordEncoder.matches("123456789", account.getPassword()));
    }


    @Test
    @DisplayName("닉네임 수정 폼")
    @WithAccount("test123")
    void updateNicknameForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @Test
    @DisplayName("닉네임 수정: 입력값 정상")
    @WithAccount("test123")
    void updateNickname() throws Exception {
        String newNickname = "test123456";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));
        Account account = accountRepository.findByNickname(newNickname);
        assertEquals(newNickname, account.getNickname());
    }

    @Test
    @DisplayName("닉네임 수정: 에러(길이)")
    @WithAccount("test123")
    void updateNicknameWithShortNickname() throws Exception {
        String newNickname = "a";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().attributeExists("account"));
    }

/*    @Test
    @DisplayName("닉네임 수정: 에러(중복)")
    @WithAccount("test123")
    void updateNicknameWithDuplicatedNickname() throws Exception {
        String newNickname = "test123";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                        .param("nickname", newNickname)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().attributeExists("account"));
    }*/

    @WithAccount("test123")
    @DisplayName("테그 수정 폼")
    @Test
    public void updateTagForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));

    }

    @WithAccount("test123")
    @DisplayName("계정에 테그 추가")
    @Test
    public void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("test123").getTags().contains(newTag));
    }

    @Test
    @DisplayName("계정에 테그 삭제")
    @WithAccount("test123")
    void removeTag() throws Exception {

        Account test123 = accountRepository.findByNickname("test123");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(test123, newTag);
        assertTrue(test123.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        String tagTitle = "newTag";
        tagForm.setTagTitle(tagTitle);
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(test123.getTags().contains(newTag));
    }

}