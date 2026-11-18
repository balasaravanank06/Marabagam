package com.marabagam.certificate;

import com.marabagam.auth.security.JwtTokenProvider;
import com.marabagam.certificate.domain.Certificate;
import com.marabagam.certificate.service.CertificateService;
import com.marabagam.learning.domain.Course;
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
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.marabagam.art.repository.ArtFormRepository artFormRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CertificateService certificateService;

    private User learner;
    private String learnerToken;
    private Certificate certificate;

    @BeforeEach
    void setUp() {
        learner = new User();
        learner.setEmail("cert_learner_" + UUID.randomUUID() + "@marabagam.org");
        learner.setUsername("cert_learner_" + UUID.randomUUID());
        learner.setPasswordHash("$2a$12$TestHashPasswordValueHere");
        learner.setDisplayName("Certificate Learner");
        learner.setRole(Role.LEARNER);
        learner.setAccountStatus(AccountStatus.ACTIVE);
        learner = userRepository.save(learner);

        learnerToken = tokenProvider.generateAccessToken(learner);

        com.marabagam.art.domain.ArtForm artForm = artFormRepository.findAll().stream().findFirst().orElseGet(() -> {
            com.marabagam.art.domain.ArtForm af = new com.marabagam.art.domain.ArtForm();
            af.setName("Silambam");
            af.setNameTa("சிலம்பம்");
            af.setSlug("silambam-" + UUID.randomUUID());
            af.setShortDescription("Traditional Tamil staff martial art");
            af.setShortDescriptionTa("பாரம்பரிய தமிழ் தற்காப்புக் கலை");
            af.setDescription("Detailed description of Silambam staff martial art.");
            af.setDescriptionTa("சிலம்பக் கலை பற்றிய விரிவான விளக்கம்.");
            af.setOrigin("Tamil Nadu");
            return artFormRepository.save(af);
        });

        Course course = courseRepository.findAll().stream().findFirst().orElseGet(() -> {
            Course c = new Course();
            c.setArtForm(artForm);
            c.setTitle("Test Silambam Course");
            c.setTitleTa("சோதனைச் சிலம்பப் பாடம்");
            c.setSlug("test-silambam-course-" + UUID.randomUUID());
            c.setShortDescription("Test course description");
            c.setShortDescriptionTa("சோதனைப் பாட விளக்கம்");
            c.setDescription("Detailed test description");
            c.setDescriptionTa("விரிவான சோதனை விளக்கம்");
            c.setDifficulty(com.marabagam.learning.domain.CourseDifficulty.BEGINNER);
            c.setStatus(com.marabagam.learning.domain.CourseStatus.PUBLISHED);
            c.setCreatedBy(learner);
            return courseRepository.save(c);
        });
        certificate = certificateService.generateCertificate(learner, course, 95.0);
    }

    @Test
    void getUserCertificates_Success() throws Exception {
        mockMvc.perform(get("/api/v1/certificates")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].certificateCode").value(certificate.getCertificateCode()));
    }

    @Test
    void publicVerifyCertificate_Success() throws Exception {
        mockMvc.perform(get("/api/v1/certificates/verify/" + certificate.getCertificateCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.authentic").value(true))
                .andExpect(jsonPath("$.data.recipientNameSnapshot").value("Certificate Learner"));
    }
}
