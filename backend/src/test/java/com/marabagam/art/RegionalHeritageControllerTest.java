package com.marabagam.art;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.domain.District;
import com.marabagam.art.domain.HeritageLocation;
import com.marabagam.art.dto.CreateHeritageLocationRequestDto;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.art.repository.HeritageLocationRepository;
import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RegionalHeritageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private HeritageLocationRepository heritageLocationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private District maduraiDistrict;
    private ArtForm silambamArtForm;
    private User artistUserA;
    private User artistUserB;
    private User culturalExpertUser;
    private User learnerUser;

    private String tokenArtistA;
    private String tokenArtistB;
    private String tokenCulturalExpert;
    private String tokenLearner;

    @BeforeEach
    public void setup() throws Exception {
        heritageLocationRepository.deleteAll();
        artFormRepository.deleteAll();
        districtRepository.deleteAll();
        userRepository.deleteAll();

        // Create District
        maduraiDistrict = new District();
        maduraiDistrict.setName("Madurai");
        maduraiDistrict.setNameTa("மதுரை");
        maduraiDistrict.setCode("TN-MAD");
        maduraiDistrict.setLatitude(9.92520);
        maduraiDistrict.setLongitude(78.11980);
        maduraiDistrict.setShortHistory("Ancient Pandyan cultural capital.");
        maduraiDistrict.setShortHistoryTa("பாண்டிய மன்னர்களின் தலை நகரம்.");
        maduraiDistrict.setGeojsonFeatureCode("TN-MAD");
        maduraiDistrict = districtRepository.save(maduraiDistrict);

        // Create Art Form
        silambamArtForm = new ArtForm();
        silambamArtForm.setName("Silambam");
        silambamArtForm.setNameTa("சிலம்பம்");
        silambamArtForm.setSlug("silambam-test");
        silambamArtForm.setShortDescription("Staff martial art");
        silambamArtForm.setShortDescriptionTa("தடி தற்காப்புக் கலை");
        silambamArtForm.setDescription("Full description");
        silambamArtForm.setDescriptionTa("முழு விளக்கம்");
        silambamArtForm.setStatus(ArtFormStatus.PUBLISHED);
        silambamArtForm.setDistricts(Set.of(maduraiDistrict));
        silambamArtForm = artFormRepository.save(silambamArtForm);

        // Create Users & Tokens
        artistUserA = createUser("artist_a", "artist_a@marabagam.org", Role.ARTIST);
        artistUserB = createUser("artist_b", "artist_b@marabagam.org", Role.ARTIST);
        culturalExpertUser = createUser("expert", "expert@marabagam.org", Role.CULTURAL_EXPERT);
        learnerUser = createUser("learner", "learner@marabagam.org", Role.LEARNER);

        tokenArtistA = obtainJwtToken("artist_a@marabagam.org", "Password123!");
        tokenArtistB = obtainJwtToken("artist_b@marabagam.org", "Password123!");
        tokenCulturalExpert = obtainJwtToken("expert@marabagam.org", "Password123!");
        tokenLearner = obtainJwtToken("learner@marabagam.org", "Password123!");
    }

    private User createUser(String username, String email, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setDisplayName(username);
        user.setRole(role);
        return userRepository.save(user);
    }

    private String obtainJwtToken(String email, String password) throws Exception {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsernameOrEmail(email);
        dto.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseJson);
        return root.path("data").path("accessToken").asText();
    }

    @Test
    public void testGetHeritageMap_ReturnsSummaryAnd200OK() throws Exception {
        mockMvc.perform(get("/api/v1/heritage-map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.state").value("Tamil Nadu"))
                .andExpect(jsonPath("$.data.totalDistricts").value(1));
    }

    @Test
    public void testSearchDistricts_ReturnsPagedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/districts/search")
                .param("query", "Madurai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].code").value("TN-MAD"));
    }

    @Test
    public void testGetDistrictById_ReturnsDetailWithPublishedArtForms() throws Exception {
        mockMvc.perform(get("/api/v1/districts/" + maduraiDistrict.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Madurai"))
                .andExpect(jsonPath("$.data.artForms[0].name").value("Silambam"));
    }

    @Test
    public void testGetDistrictsByArtForm_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/v1/art-forms/silambam-test/districts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("TN-MAD"));
    }

    @Test
    public void testProposeLocation_Artist_CreatesDraft_Succeeds() throws Exception {
        CreateHeritageLocationRequestDto req = new CreateHeritageLocationRequestDto();
        req.setName("Madurai Silambam Academy");
        req.setNameTa("மதுரை சிலம்பக் கழகம்");
        req.setDistrictId(maduraiDistrict.getId());
        req.setArtFormId(silambamArtForm.getId());
        req.setLocationType("TRAINING_CENTER");
        req.setDescription("Authentic academy");
        req.setDescriptionTa("பாரம்பரிய மையம்");
        req.setAddress("Tallakulam");
        req.setAddressTa("தல்லாகுளம்");
        req.setLatitude(9.92520);
        req.setLongitude(78.11980);
        req.setProvenanceSource("Federation Directory");

        mockMvc.perform(post("/api/v1/heritage-locations")
                .header("Authorization", "Bearer " + tokenArtistA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    public void testProposeLocation_Guest_Returns401() throws Exception {
        CreateHeritageLocationRequestDto req = new CreateHeritageLocationRequestDto();
        req.setName("Unauthenticated Academy");

        mockMvc.perform(post("/api/v1/heritage-locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testProposeLocation_Learner_Returns403() throws Exception {
        CreateHeritageLocationRequestDto req = new CreateHeritageLocationRequestDto();
        req.setName("Learner Academy Attempt");
        req.setNameTa("கற்பவர் முயற்சி");
        req.setDistrictId(maduraiDistrict.getId());
        req.setLocationType("TRAINING_CENTER");
        req.setDescription("Desc");
        req.setDescriptionTa("விளக்கம்");
        req.setAddress("Address");
        req.setAddressTa("முகவரி");
        req.setLatitude(9.92520);
        req.setLongitude(78.11980);

        mockMvc.perform(post("/api/v1/heritage-locations")
                .header("Authorization", "Bearer " + tokenLearner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testProposeLocation_InvalidCoordinates_Returns400() throws Exception {
        CreateHeritageLocationRequestDto req = new CreateHeritageLocationRequestDto();
        req.setName("Out of Bounds Academy");
        req.setNameTa("எல்லை தாண்டிய மையம்");
        req.setDistrictId(maduraiDistrict.getId());
        req.setLocationType("TRAINING_CENTER");
        req.setDescription("Desc");
        req.setDescriptionTa("விளக்கம்");
        req.setAddress("Address");
        req.setAddressTa("முகவரி");
        req.setLatitude(25.00000); // Out of TN bounds
        req.setLongitude(78.00000);

        mockMvc.perform(post("/api/v1/heritage-locations")
                .header("Authorization", "Bearer " + tokenArtistA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateLocation_IDOR_Blocked_ForOtherArtist_Returns403() throws Exception {
        // Create draft by Artist A
        CreateHeritageLocationRequestDto req = new CreateHeritageLocationRequestDto();
        req.setName("Artist A Academy");
        req.setNameTa("கலைஞர் ஏ மையம்");
        req.setDistrictId(maduraiDistrict.getId());
        req.setLocationType("TRAINING_CENTER");
        req.setDescription("Desc");
        req.setDescriptionTa("விளக்கம்");
        req.setAddress("Address");
        req.setAddressTa("முகவரி");
        req.setLatitude(9.92520);
        req.setLongitude(78.11980);

        MvcResult result = mockMvc.perform(post("/api/v1/heritage-locations")
                .header("Authorization", "Bearer " + tokenArtistA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String locId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Artist B attempts to update Artist A's draft -> 403 Forbidden
        req.setName("Artist B Hijack Attempt");
        mockMvc.perform(put("/api/v1/heritage-locations/" + locId)
                .header("Authorization", "Bearer " + tokenArtistB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateStatus_CulturalExpert_Succeeds() throws Exception {
        // Create location draft by Artist A
        CreateHeritageLocationRequestDto req = new CreateHeritageLocationRequestDto();
        req.setName("Expert Review Academy");
        req.setNameTa("மதிப்பீட்டு மையம்");
        req.setDistrictId(maduraiDistrict.getId());
        req.setLocationType("TRAINING_CENTER");
        req.setDescription("Desc");
        req.setDescriptionTa("விளக்கம்");
        req.setAddress("Address");
        req.setAddressTa("முகவரி");
        req.setLatitude(9.92520);
        req.setLongitude(78.11980);

        MvcResult result = mockMvc.perform(post("/api/v1/heritage-locations")
                .header("Authorization", "Bearer " + tokenArtistA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String locId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Cultural Expert approves status
        Map<String, String> statusPayload = Map.of("status", "PUBLISHED");
        mockMvc.perform(put("/api/v1/heritage-locations/" + locId + "/status")
                .header("Authorization", "Bearer " + tokenCulturalExpert)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    public void testArchivedLocation_DisappearsFromPublicMap() throws Exception {
        // Create location & publish
        CreateHeritageLocationRequestDto req = new CreateHeritageLocationRequestDto();
        req.setName("Archival Test Academy");
        req.setNameTa("ஆவணக் காப்பக மையம்");
        req.setDistrictId(maduraiDistrict.getId());
        req.setLocationType("TRAINING_CENTER");
        req.setDescription("Desc");
        req.setDescriptionTa("விளக்கம்");
        req.setAddress("Address");
        req.setAddressTa("முகவரி");
        req.setLatitude(9.92520);
        req.setLongitude(78.11980);

        MvcResult result = mockMvc.perform(post("/api/v1/heritage-locations")
                .header("Authorization", "Bearer " + tokenArtistA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String locId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // Publish location
        mockMvc.perform(put("/api/v1/heritage-locations/" + locId + "/status")
                .header("Authorization", "Bearer " + tokenCulturalExpert)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "PUBLISHED"))))
                .andExpect(status().isOk());

        // Archive location
        mockMvc.perform(put("/api/v1/heritage-locations/" + locId + "/archive")
                .header("Authorization", "Bearer " + tokenCulturalExpert))
                .andExpect(status().isOk());

        // Public API details check: location should NOT be present in published list
        mockMvc.perform(get("/api/v1/districts/" + maduraiDistrict.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locations").isEmpty());
    }
}
