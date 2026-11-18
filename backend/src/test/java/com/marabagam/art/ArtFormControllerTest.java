package com.marabagam.art;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.domain.Category;
import com.marabagam.art.domain.District;
import com.marabagam.art.dto.CreateArtFormRequestDto;
import com.marabagam.art.dto.UpdateArtFormStatusRequestDto;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.CategoryRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.auth.dto.RegisterRequestDto;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ArtFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Category martialCategory;
    private District maduraiDistrict;

    @BeforeEach
    public void setUp() {
        artFormRepository.deleteAll();
        categoryRepository.deleteAll();
        districtRepository.deleteAll();
        userRepository.deleteAll();

        martialCategory = new Category();
        martialCategory.setName("Martial Arts");
        martialCategory.setNameTa("தற்காப்புக் கலைகள்");
        martialCategory = categoryRepository.save(martialCategory);

        maduraiDistrict = new District();
        maduraiDistrict.setName("Madurai");
        maduraiDistrict.setNameTa("மதுரை");
        maduraiDistrict = districtRepository.save(maduraiDistrict);
    }

    @Test
    public void testPublicApiReturnsOnlyPublishedArtForms() throws Exception {
        ArtForm published = createTestArtForm("Silambam", "silambam", ArtFormStatus.PUBLISHED);
        ArtForm draft = createTestArtForm("Draft Art", "draft-art", ArtFormStatus.DRAFT);

        mockMvc.perform(get("/api/v1/art-forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("silambam"));
    }

    @Test
    public void testSearchAndFilterPublishedArtForms() throws Exception {
        Category folkCategory = new Category();
        folkCategory.setName("Folk Dance");
        folkCategory.setNameTa("கிராமிய நடனங்கள்");
        folkCategory = categoryRepository.save(folkCategory);

        ArtForm silambam = createTestArtForm("Silambam", "silambam", ArtFormStatus.PUBLISHED);

        ArtForm karagattam = new ArtForm();
        karagattam.setName("Karagattam");
        karagattam.setNameTa("கரகாட்டம்");
        karagattam.setSlug("karagattam");
        karagattam.setShortDescription("Short description for Karagattam");
        karagattam.setShortDescriptionTa("குறுகிய விளக்கம்");
        karagattam.setDescription("Full description for Karagattam");
        karagattam.setDescriptionTa("முழு விளக்கம்");
        karagattam.setStatus(ArtFormStatus.PUBLISHED);
        karagattam.setCategories(Set.of(folkCategory));
        karagattam.setDistricts(Set.of(maduraiDistrict));
        artFormRepository.save(karagattam);

        // Search test
        mockMvc.perform(get("/api/v1/art-forms?search=silambam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Silambam"));

        // Category filter test
        mockMvc.perform(get("/api/v1/art-forms?category=Martial Arts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Silambam"));
    }

    @Test
    public void testLearnerCannotCreateArtForm() throws Exception {
        String learnerToken = getAccessTokenForRole(Role.LEARNER, "learner@marabagam.org");

        CreateArtFormRequestDto request = createSampleRequestDto("Oyillatam", "oyilattam");

        mockMvc.perform(post("/api/v1/admin/art-forms")
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testArtistCanCreateDraftAndAdminCanPublish() throws Exception {
        String artistToken = getAccessTokenForRole(Role.ARTIST, "artist@marabagam.org");
        String adminToken = getAccessTokenForRole(Role.ADMIN, "admin@marabagam.org");

        // Artist Creates DRAFT
        CreateArtFormRequestDto createReq = createSampleRequestDto("Devarattam", "devarattam");

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/art-forms")
                        .header("Authorization", "Bearer " + artistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        String idStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // Admin Publishes Content
        UpdateArtFormStatusRequestDto statusReq = new UpdateArtFormStatusRequestDto(ArtFormStatus.PUBLISHED);

        mockMvc.perform(patch("/api/v1/admin/art-forms/" + idStr + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // Now accessible publicly
        mockMvc.perform(get("/api/v1/art-forms/devarattam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Devarattam"));
    }

    private ArtForm createTestArtForm(String name, String slug, ArtFormStatus status) {
        ArtForm a = new ArtForm();
        a.setName(name);
        a.setNameTa(name + " தமிழ்");
        a.setSlug(slug);
        a.setShortDescription("Short description for " + name);
        a.setShortDescriptionTa("குறுகிய விளக்கம்");
        a.setDescription("Full description for " + name);
        a.setDescriptionTa("முழு விளக்கம்");
        a.setStatus(status);
        a.setCategories(Set.of(martialCategory));
        a.setDistricts(Set.of(maduraiDistrict));
        return artFormRepository.save(a);
    }

    private CreateArtFormRequestDto createSampleRequestDto(String name, String slug) {
        CreateArtFormRequestDto dto = new CreateArtFormRequestDto();
        dto.setName(name);
        dto.setNameTa(name + " தமிழ்");
        dto.setSlug(slug);
        dto.setShortDescription("Short description");
        dto.setShortDescriptionTa("குறுகிய விளக்கம்");
        dto.setDescription("Full description");
        dto.setDescriptionTa("முழு விளக்கம்");
        dto.setCategoryIds(Set.of(martialCategory.getId()));
        dto.setDistrictIds(Set.of(maduraiDistrict.getId()));
        return dto;
    }

    private String getAccessTokenForRole(Role role, String email) throws Exception {
        User u = new User();
        u.setEmail(email);
        u.setUsername(email.split("@")[0]);
        u.setPasswordHash(passwordEncoder.encode("Password123!"));
        u.setDisplayName("Test " + role.name());
        u.setPreferredLanguage("en");
        u.setRole(role);
        userRepository.save(u);

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsernameOrEmail(email);
        loginDto.setPassword("Password123!");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }
}
