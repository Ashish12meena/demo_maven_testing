package com.grras.testing.unit;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.grras.testing.entity.Student;
import com.grras.testing.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ============================================================
 *  REPOSITORY LAYER TEST — StudentRepository
 * ============================================================
 *
 *  @DataJpaTest
 *      → Loads ONLY JPA components (Entities, Repositories).
 *        Does NOT load @Service or @Controller beans.
 *        Uses H2 in-memory DB automatically (no MySQL needed).
 *        Each test is @Transactional + auto rolled back.
 *
 *  TestEntityManager
 *      → Provided by Spring for test data setup.
 *        Use this to persist test data instead of the repository.
 *
 *  This layer tests:
 *      - Custom derived query methods
 *      - Custom @Query JPQL methods
 *      - Native queries
 * ============================================================
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("StudentRepository Tests")
class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;    // ← For setting up test data

    @Autowired
    private StudentRepository studentRepository;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        // Persist test data using TestEntityManager (not repository)
        student1 = entityManager.persistAndFlush(
            Student.builder()
                .name("Ashish Sharma")
                .email("ashish@example.com")
                .grade(8)
                .build()
        );

        student2 = entityManager.persistAndFlush(
            Student.builder()
                .name("Mehak Gupta")
                .email("mehak@example.com")
                .grade(9)
                .build()
        );
    }

    @Test
    @DisplayName("findByEmail - should return student when email exists")
    void shouldFindByEmail() {
        Optional<Student> found = studentRepository.findByEmail("ashish@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Ashish Sharma");
    }

    @Test
    @DisplayName("findByEmail - should return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<Student> found = studentRepository.findByEmail("notexist@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail - should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        boolean exists = studentRepository.existsByEmail("ashish@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - should return false when email not found")
    void shouldReturnFalseWhenEmailNotFound() {
        boolean exists = studentRepository.existsByEmail("nobody@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByGrade - should return students with matching grade")
    void shouldFindByGrade() {
        List<Student> grade8Students = studentRepository.findByGrade(8);

        assertThat(grade8Students).hasSize(1);
        assertThat(grade8Students.get(0).getName()).isEqualTo("Ashish Sharma");
    }

    @Test
    @DisplayName("searchByName (JPQL) - should find students with matching keyword")
    void shouldSearchByName() {
        List<Student> results = studentRepository.searchByName("Ashish");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("ashish@example.com");
    }

    @Test
    @DisplayName("findStudentsWithMinGrade (Native) - should return students above min grade")
    void shouldFindStudentsWithMinGrade() {
        List<Student> results = studentRepository.findStudentsWithMinGrade(8);

        assertThat(results).hasSize(2); // grade 8 and 9 both qualify
    }
}
