package com.marabagam.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.learning.domain.*;
import com.marabagam.learning.dto.*;
import com.marabagam.learning.repository.*;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User artistUser;
    private User otherArtistUser;
    private User expertUser;
    private User learnerUser;

    private ArtForm publishedArtForm;
    private ArtForm draftArtForm;

    private Course publishedCourse;
    private Course draftCourse;

    private Lesson publishedLesson;
    private Lesson draftLesson;

    @BeforeEach
    public void setUp() {
        courseRepository.deleteAll();
        artFormRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        adminUser = createUser("admin_l@marabagam.org", "admin_l", Role.ADMIN);
        artistUser = createUser("artist1_l@marabagam.org", "artist1_l", Role.ARTIST);
        otherArtistUser = createUser("artist2_l@marabagam.org", "artist2_l", Role.ARTIST);
        expertUser = createUser("expert_l@marabagam.org", "expert_l", Role.CULTURAL_EXPERT);
        learnerUser = createUser("learner1_l@marabagam.org", "learner1_l", Role.LEARNER);

        // Create Art Forms
        publishedArtForm = new ArtForm();
        publishedArtForm.setName("Silambam Fencing");
        publishedArtForm.setNameTa("சிலம்பம்");
        publishedArtForm.setSlug("silambam-fencing-" + UUID.randomUUID().toString().substring(0, 6));
        publishedArtForm.setShortDescription("Staff martial art");
        publishedArtForm.setShortDescriptionTa("தற்காப்பு கலை");
        publishedArtForm.setDescription("Ancient martial art");
        publishedArtForm.setDescriptionTa("பண்டைய கலை");
        publishedArtForm.setStatus(ArtFormStatus.PUBLISHED);
        publishedArtForm = artFormRepository.save(publishedArtForm);

        draftArtForm = new ArtForm();
        draftArtForm.setName("Unpublished Art");
        draftArtForm.setNameTa("வெளிவராத கலை");
        draftArtForm.setSlug("unpublished-art-" + UUID.randomUUID().toString().substring(0, 6));
        draftArtForm.setShortDescription("Draft art form");
        draftArtForm.setShortDescriptionTa("வரைவு கலை");
        draftArtForm.setDescription("Draft art form description");
        draftArtForm.setDescriptionTa("வரைவு விளக்கம்");
        draftArtForm.setStatus(ArtFormStatus.DRAFT);
        draftArtForm = artFormRepository.save(draftArtForm);

        // Create Published Course
        publishedCourse = new Course();
        publishedCourse.setArtForm(publishedArtForm);
        publishedCourse.setTitle("Mastering Silambam Spins");
        publishedCourse.setTitleTa("சிலம்ப சுழற்றல்கள்");
        publishedCourse.setSlug("mastering-silambam-" + UUID.randomUUID().toString().substring(0, 6));
        publishedCourse.setShortDescription("Learn Kamai and footwork");
        publishedCourse.setShortDescriptionTa("கால்வரிசை கற்க");
        publishedCourse.setDescription("Detailed course description");
        publishedCourse.setDescriptionTa("விரிவான பாடம்");
        publishedCourse.setDifficulty(CourseDifficulty.BEGINNER);
        publishedCourse.setEstimatedDurationMinutes(60);
        publishedCourse.setStatus(CourseStatus.PUBLISHED);
        publishedCourse.setCreatedBy(adminUser);
        publishedCourse = courseRepository.save(publishedCourse);

        // Create Draft Course
        draftCourse = new Course();
        draftCourse.setArtForm(publishedArtForm);
        draftCourse.setTitle("Draft Secret Course");
        draftCourse.setTitleTa("வரைவு பாடம்");
        draftCourse.setSlug("draft-secret-course-" + UUID.randomUUID().toString().substring(0, 6));
        draftCourse.setShortDescription("Draft course");
        draftCourse.setShortDescriptionTa("வரைவு விளக்கம்");
        draftCourse.setDescription("Draft course content");
        draftCourse.setDescriptionTa("வரைவு உள்ளடக்கம்");
        draftCourse.setDifficulty(CourseDifficulty.ADVANCED);
        draftCourse.setStatus(CourseStatus.DRAFT);
        draftCourse.setCreatedBy(artistUser);
        draftCourse = courseRepository.save(draftCourse);

        // Create Module and Lessons
        CourseModule module = new CourseModule();
        module.setCourse(publishedCourse);
        module.setTitle("Module 1: Basics");
        module.setTitleTa("அலகு 1");
        module.setDisplayOrder(1);
        module = moduleRepository.save(module);

        publishedLesson = new Lesson();
        publishedLesson.setModule(module);
        publishedLesson.setTitle("Lesson 1: Grip and Stance");
        publishedLesson.setTitleTa("பாடம் 1");
        publishedLesson.setLessonType(LessonType.TEXT);
        publishedLesson.setDisplayOrder(1);
        publishedLesson.setStatus(LessonStatus.PUBLISHED);
        publishedLesson.setCreatedBy(adminUser);
        publishedLesson = lessonRepository.save(publishedLesson);

        draftLesson = new Lesson();
        draftLesson.setModule(module);
        draftLesson.setTitle("Lesson 2: Secret Move");
        draftLesson.setTitleTa("பாடம் 2");
        draftLesson.setLessonType(LessonType.VIDEO);
        draftLesson.setDisplayOrder(2);
        draftLesson.setStatus(LessonStatus.DRAFT);
        draftLesson.setCreatedBy(artistUser);
        draftLesson = lessonRepository.save(draftLesson);
    }

    private User createUser(String email, String username, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setDisplayName("Test " + username);
        user.setPreferredLanguage("en");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Autowired
    private com.marabagam.auth.security.JwtTokenProvider tokenProvider;

    private String getAccessToken(User user) {
        return tokenProvider.generateAccessToken(user);
    }

    // 1. Published courses visible publicly
    @Test
    public void testGetPublicCourses_ReturnsOnlyPublished() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[*].slug", hasItem(publishedCourse.getSlug())))
                .andExpect(jsonPath("$.data.content[*].slug", not(hasItem(draftCourse.getSlug()))));
    }

    // 2. Draft courses hidden publicly
    @Test
    public void testGetPublicCourseBySlug_DraftNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/courses/" + draftCourse.getSlug()))
                .andExpect(status().isNotFound());
    }

    // 3. Unpublished lessons hidden publicly & IDOR cross-course protection
    @Test
    public void testGetPublicLesson_UnpublishedIsolated() throws Exception {
        mockMvc.perform(get("/api/v1/courses/" + publishedCourse.getSlug() + "/lessons/" + draftLesson.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetPublicLesson_PublishedAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/courses/" + publishedCourse.getSlug() + "/lessons/" + publishedLesson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Lesson 1: Grip and Stance"));
    }

    // 4. Search works
    @Test
    public void testGetPublicCourses_SearchFilter() throws Exception {
        mockMvc.perform(get("/api/v1/courses").param("search", "Silambam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].title", hasItem("Mastering Silambam Spins")));
    }

    // 5. Learner enrollment & Duplicate handling & Soft cancellation
    @Test
    public void testEnrollmentFlowAndSoftCancellation() throws Exception {
        String token = getAccessToken(learnerUser);

        // Enroll
        mockMvc.perform(post("/api/v1/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Duplicate Enrollment (Returns existing ACTIVE enrollment)
        mockMvc.perform(post("/api/v1/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Soft Cancellation
        mockMvc.perform(delete("/api/v1/users/me/enrollments/" + publishedCourse.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Verify enrollment record remains persisted in DB
        assertTrue(enrollmentRepository.findByUserIdAndCourseId(learnerUser.getId(), publishedCourse.getId()).isPresent());
        assertEquals(EnrollmentStatus.CANCELLED, enrollmentRepository.findByUserIdAndCourseId(learnerUser.getId(), publishedCourse.getId()).get().getStatus());
    }

    // 6. Learner progress & Course Completion Rule
    @Test
    public void testProgressUpdateAndCourseCompletion() throws Exception {
        String token = getAccessToken(learnerUser);

        // First enroll
        mockMvc.perform(post("/api/v1/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Update progress to 100%
        UpdateLessonProgressRequestDto req = new UpdateLessonProgressRequestDto();
        req.setProgressPercent(100);

        mockMvc.perform(put("/api/v1/lessons/" + publishedLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        // Verify Course Completion (since publishedLesson is the only published lesson in publishedCourse)
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(learnerUser.getId(), publishedCourse.getId()).orElseThrow();
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
        assertNotNull(enrollment.getCompletedAt());
    }

    // 7. Invalid progress rejected: completed=true with progressPercent < 100
    @Test
    public void testInvalidProgress_CompletedWithSub100Rejected() throws Exception {
        String token = getAccessToken(learnerUser);

        UpdateLessonProgressRequestDto req = new UpdateLessonProgressRequestDto();
        req.setProgressPercent(50);
        req.setCompleted(true);

        mockMvc.perform(put("/api/v1/lessons/" + publishedLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 8. Contributor Ownership Checks (Artist cannot publish, cannot edit another's draft)
    @Test
    public void testArtistCannotEditOtherContributorsDraft() throws Exception {
        String otherArtistToken = getAccessToken(otherArtistUser);

        UpdateCourseRequestDto updateReq = new UpdateCourseRequestDto();
        updateReq.setTitle("Hacked Title");
        updateReq.setTitleTa("ஹேக் செய்யப்பட்டது");
        updateReq.setSlug(draftCourse.getSlug());
        updateReq.setShortDescription("Short");
        updateReq.setShortDescriptionTa("குறுகிய");
        updateReq.setDescription("Long");
        updateReq.setDescriptionTa("நீண்ட");
        updateReq.setDifficulty(CourseDifficulty.BEGINNER);

        mockMvc.perform(put("/api/v1/admin/courses/" + draftCourse.getId())
                        .header("Authorization", "Bearer " + otherArtistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testArtistCannotPublish() throws Exception {
        String artistToken = getAccessToken(artistUser);

        UpdateCourseStatusRequestDto statusReq = new UpdateCourseStatusRequestDto();
        statusReq.setStatus(CourseStatus.PUBLISHED);

        mockMvc.perform(patch("/api/v1/admin/courses/" + draftCourse.getId() + "/status")
                        .header("Authorization", "Bearer " + artistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden());
    }

    // 9. Cultural Expert approval & Admin publication
    @Test
    public void testCulturalExpertCanApprove() throws Exception {
        String expertToken = getAccessToken(expertUser);

        UpdateCourseStatusRequestDto statusReq = new UpdateCourseStatusRequestDto();
        statusReq.setStatus(CourseStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/admin/courses/" + draftCourse.getId() + "/status")
                        .header("Authorization", "Bearer " + expertToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testAdminCanPublish() throws Exception {
        String adminToken = getAccessToken(adminUser);

        UpdateCourseStatusRequestDto statusReq = new UpdateCourseStatusRequestDto();
        statusReq.setStatus(CourseStatus.PUBLISHED);

        mockMvc.perform(patch("/api/v1/admin/courses/" + draftCourse.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    public void testUnpublishedArtForm_CourseInaccessible() throws Exception {
        // Create published course under draft (unpublished) art form
        Course courseUnderDraftArt = new Course();
        courseUnderDraftArt.setArtForm(draftArtForm);
        courseUnderDraftArt.setTitle("Draft Art Course");
        courseUnderDraftArt.setTitleTa("வரைவு கலைப் பாடம்");
        courseUnderDraftArt.setSlug("draft-art-course-" + UUID.randomUUID().toString().substring(0, 6));
        courseUnderDraftArt.setShortDescription("Short");
        courseUnderDraftArt.setShortDescriptionTa("குறுகிய");
        courseUnderDraftArt.setDescription("Long");
        courseUnderDraftArt.setDescriptionTa("நீண்ட");
        courseUnderDraftArt.setDifficulty(CourseDifficulty.BEGINNER);
        courseUnderDraftArt.setStatus(CourseStatus.PUBLISHED);
        courseUnderDraftArt.setCreatedBy(adminUser);
        courseRepository.save(courseUnderDraftArt);

        // Accessing public course by slug must fail (404) because parent art form is DRAFT
        mockMvc.perform(get("/api/v1/courses/" + courseUnderDraftArt.getSlug()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCrossCourseLessonAccess_Blocked() throws Exception {
        // Create Course B
        Course courseB = new Course();
        courseB.setArtForm(publishedArtForm);
        courseB.setTitle("Second Course");
        courseB.setTitleTa("இரண்டாம் பாடம்");
        courseB.setSlug("second-course-" + UUID.randomUUID().toString().substring(0, 6));
        courseB.setShortDescription("Short");
        courseB.setShortDescriptionTa("குறுகிய");
        courseB.setDescription("Long");
        courseB.setDescriptionTa("நீண்ட");
        courseB.setDifficulty(CourseDifficulty.INTERMEDIATE);
        courseB.setStatus(CourseStatus.PUBLISHED);
        courseB.setCreatedBy(adminUser);
        courseB = courseRepository.save(courseB);

        // Attempting to access publishedLesson (which belongs to publishedCourse) via courseB slug must fail (404)
        mockMvc.perform(get("/api/v1/courses/" + courseB.getSlug() + "/lessons/" + publishedLesson.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testInstructorCannotPublish() throws Exception {
        User instructorUser = createUser("instructor1_l@marabagam.org", "instructor1_l", Role.INSTRUCTOR);
        String instructorToken = getAccessToken(instructorUser);

        UpdateCourseStatusRequestDto statusReq = new UpdateCourseStatusRequestDto();
        statusReq.setStatus(CourseStatus.PUBLISHED);

        mockMvc.perform(patch("/api/v1/admin/courses/" + draftCourse.getId() + "/status")
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLearnerCannotModifyAnotherUsersEnrollment() throws Exception {
        User learner2 = createUser("learner2_l@marabagam.org", "learner2_l", Role.LEARNER);
        String token1 = getAccessToken(learnerUser);
        String token2 = getAccessToken(learner2);

        // Enroll both learners in publishedCourse
        mockMvc.perform(post("/api/v1/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/courses/" + publishedCourse.getId() + "/enroll")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        // Learner 2 cancels their enrollment
        mockMvc.perform(delete("/api/v1/users/me/enrollments/" + publishedCourse.getId())
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());

        // Learner 1's enrollment remains ACTIVE
        Enrollment enrollment1 = enrollmentRepository.findByUserIdAndCourseId(learnerUser.getId(), publishedCourse.getId()).orElseThrow();
        assertEquals(EnrollmentStatus.ACTIVE, enrollment1.getStatus());
    }
}
