package com.marabagam.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.domain.Category;
import com.marabagam.art.domain.District;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.CategoryRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.learning.domain.Course;
import com.marabagam.learning.domain.CourseDifficulty;
import com.marabagam.learning.domain.CourseStatus;
import com.marabagam.learning.repository.CourseRepository;
import com.marabagam.user.domain.AccountStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

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

    private User learner;
    private String learnerToken;

    @BeforeEach
    public void setUp() throws Exception {
        courseRepository.deleteAll();
        artFormRepository.deleteAll();
        categoryRepository.deleteAll();
        districtRepository.deleteAll();
        userRepository.deleteAll();

        Category folkCat = new Category();
        folkCat.setName("Folk Dance");
        folkCat.setNameTa("கிராமிய நடனங்கள்");
        folkCat = categoryRepository.save(folkCat);

        District madurai = new District();
        madurai.setName("Madurai");
        madurai.setNameTa("மதுரை");
        madurai = districtRepository.save(madurai);

        ArtForm silambam = new ArtForm();
        silambam.setName("Silambam");
        silambam.setNameTa("சிலம்பம்");
        silambam.setSlug("silambam");
        silambam.setShortDescription("Silambam short description");
        silambam.setShortDescriptionTa("சிலம்பம் சுருக்கம்");
        silambam.setDescription("Silambam full description");
        silambam.setDescriptionTa("சிலம்பம் முழு விளக்கம்");
        silambam.setCategory(folkCat);
        silambam.setOriginDistrict(madurai);
        silambam.setStatus(ArtFormStatus.PUBLISHED);
        silambam = artFormRepository.save(silambam);

        learner = new User();
        learner.setEmail("learner@test.com");
        learner.setUsername("learner");
        learner.setDisplayName("Learner User");
        learner.setPasswordHash(passwordEncoder.encode("password123"));
        learner.setRole(Role.LEARNER);
        learner.setAccountStatus(AccountStatus.ACTIVE);
        learner = userRepository.save(learner);

        Course c1 = new Course();
        c1.setTitle("Silambam Basics");
        c1.setTitleTa("சிலம்பம் அடிப்படை");
        c1.setSlug("silambam-basics");
        c1.setShortDescription("Short desc");
        c1.setShortDescriptionTa("சுருக்கம்");
        c1.setDescription("Full description");
        c1.setDescriptionTa("முழு விளக்கம்");
        c1.setArtForm(silambam);
        c1.setDifficulty(CourseDifficulty.BEGINNER);
        c1.setStatus(CourseStatus.PUBLISHED);
        c1.setCreatedBy(learner);
        courseRepository.save(c1);

        LoginRequestDto req = new LoginRequestDto();
        req.setUsernameOrEmail("learner@test.com");
        req.setPassword("password123");
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        learnerToken = objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    @Test
    public void testRecommendationFeedGenerationWithBilingualExplanations() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                .header("Authorization", "Bearer " + learnerToken)
                .param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].reason").exists())
                .andExpect(jsonPath("$.data.items[0].reasonTa").exists());
    }
}
