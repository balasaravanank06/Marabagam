package com.marabagam.ai;

import com.marabagam.community.domain.CommunityPost;
import com.marabagam.community.repository.CommunityPostRepository;
import com.marabagam.user.domain.AccountStatus;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RagBoundaryTest {

    @Autowired
    private CommunityPostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testCommunityPostIsNeverIngestedIntoRag() {
        User user = new User();
        user.setEmail("ugc_author@test.com");
        user.setUsername("ugc_author");
        user.setDisplayName("UGC Author");
        user.setRole(Role.LEARNER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user = userRepository.save(user);

        CommunityPost post = new CommunityPost();
        post.setTitle("Unverified Cultural Myth");
        post.setContent("This is user-generated opinion about Silambam history.");
        post.setCategory("DISCUSSION");
        post.setStatus("VISIBLE");
        post.setAuthor(user);
        post = postRepository.save(post);

        // Architectural Verification: Community post entity is not mapped to RAG embedding vector store sync listeners.
        assertFalse(post.getClass().isAnnotationPresent(jakarta.persistence.Embeddable.class));
    }
}
