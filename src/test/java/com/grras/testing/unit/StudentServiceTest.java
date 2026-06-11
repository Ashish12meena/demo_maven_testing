package com.grras.testing.unit;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grras.testing.entity.Student;
import com.grras.testing.exception.DuplicateEmailException;
import com.grras.testing.exception.StudentNotFoundException;
import com.grras.testing.repository.StudentRepository;
import com.grras.testing.service.StudentService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 *  UNIT TESTS — StudentService
 * ============================================================
 *
 * KEY ANNOTATIONS:
 *
 *  @ExtendWith(MockitoExtension.class)
 *      → Activates Mockito in JUnit 5. No Spring context is loaded.
 *        This makes tests FAST (no DB, no HTTP).
 *
 *  @Mock
 *      → Creates a fake (mock) instance of StudentRepository.
 *        None of its methods actually hit the DB.
 *
 *  @InjectMocks
 *      → Creates a real StudentService and INJECTS the @Mock into it.
 *        So service.studentRepository = mocked repo.
 *
 *  @Test        → marks a method as a test case
 *  @BeforeEach  → runs before EACH test method
 *  @AfterEach   → runs after EACH test method
 *  @BeforeAll   → runs ONCE before all tests in this class (must be static)
 *  @AfterAll    → runs ONCE after all tests in this class (must be static)
 *  @Nested      → groups related tests into inner classes
 *  @DisplayName → gives a human-readable test name
 * ============================================================
 */
@ExtendWith(MockitoExtension.class)         // ← KEY: Activates Mockito, no Spring context
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    @Mock                                   // ← Fake repository, no DB
    private StudentRepository studentRepository;

    @InjectMocks                            // ← Real service with mocked repo injected
    private StudentService studentService;

    // --------------------------------------------------------
    // Test data reused across tests
    // --------------------------------------------------------
    private Student student1;
    private Student student2;

    @BeforeAll                              // ← Runs once before all tests in this class
    static void initAll() {
        System.out.println("=== StudentServiceTest STARTED ===");
    }

    @BeforeEach                             // ← Runs before each individual test
    void setUp() {
        student1 = Student.builder()
                .id(1L)
                .name("Ashish Sharma")
                .email("ashish@example.com")
                .grade(8)
                .build();

        student2 = Student.builder()
                .id(2L)
                .name("Mehak Gupta")
                .email("mehak@example.com")
                .grade(9)
                .build();
    }

    @AfterEach                              // ← Runs after each individual test
    void tearDown() {
        // Clean up if needed
    }

    @AfterAll                               // ← Runs once after all tests in this class
    static void cleanUpAll() {
        System.out.println("=== StudentServiceTest FINISHED ===");
    }

    // ============================================================
    //  @Nested — groups tests for createStudent()
    // ============================================================
    @Nested
    @DisplayName("createStudent() tests")
    class CreateStudentTests {

        @Test
        @DisplayName("Should create student successfully when email is unique")
        void shouldCreateStudentSuccessfully() {
            // ARRANGE — set up mock behavior
            when(studentRepository.existsByEmail("ashish@example.com")).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenReturn(student1);

            // ACT — call the actual service method
            Student result = studentService.createStudent(student1);

            // ASSERT — verify expected outcome
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Ashish Sharma");

            // VERIFY — ensure repository methods were called correctly
            verify(studentRepository, times(1)).existsByEmail("ashish@example.com");
            verify(studentRepository, times(1)).save(student1);
        }

        @Test
        @DisplayName("Should throw DuplicateEmailException when email already exists")
        void shouldThrowDuplicateEmailException() {
            // ARRANGE
            when(studentRepository.existsByEmail("ashish@example.com")).thenReturn(true);

            // ACT + ASSERT — assertThrows checks that exception IS thrown
            assertThatThrownBy(() -> studentService.createStudent(student1))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("ashish@example.com");

            // VERIFY — save should NEVER be called if email already exists
            verify(studentRepository, never()).save(any(Student.class));
        }
    }

    // ============================================================
    //  @Nested — groups tests for getStudentById()
    // ============================================================
    @Nested
    @DisplayName("getStudentById() tests")
    class GetStudentByIdTests {

        @Test
        @DisplayName("Should return student when found by ID")
        void shouldReturnStudentWhenFound() {
            // ARRANGE
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));

            // ACT
            Student result = studentService.getStudentById(1L);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("ashish@example.com");
        }

        @Test
        @DisplayName("Should throw StudentNotFoundException when ID not found")
        void shouldThrowStudentNotFoundException() {
            // ARRANGE
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            // ACT + ASSERT
            assertThatThrownBy(() -> studentService.getStudentById(99L))
                    .isInstanceOf(StudentNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ============================================================
    //  @Nested — groups tests for getAllStudents()
    // ============================================================
    @Nested
    @DisplayName("getAllStudents() tests")
    class GetAllStudentsTests {

        @Test
        @DisplayName("Should return list of all students")
        void shouldReturnAllStudents() {
            // ARRANGE
            when(studentRepository.findAll()).thenReturn(Arrays.asList(student1, student2));

            // ACT
            List<Student> result = studentService.getAllStudents();

            // ASSERT
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Student::getName)
                    .containsExactlyInAnyOrder("Ashish Sharma", "Mehak Gupta");
        }

        @Test
        @DisplayName("Should return empty list when no students exist")
        void shouldReturnEmptyList() {
            // ARRANGE
            when(studentRepository.findAll()).thenReturn(List.of());

            // ACT
            List<Student> result = studentService.getAllStudents();

            // ASSERT
            assertThat(result).isEmpty();
        }
    }

    // ============================================================
    //  @Nested — groups tests for deleteStudent()
    // ============================================================
    @Nested
    @DisplayName("deleteStudent() tests")
    class DeleteStudentTests {

        @Test
        @DisplayName("Should delete student successfully when found")
        void shouldDeleteStudentSuccessfully() {
            // ARRANGE
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
            doNothing().when(studentRepository).delete(student1);

            // ACT
            studentService.deleteStudent(1L);

            // ASSERT + VERIFY
            verify(studentRepository, times(1)).delete(student1);
        }

        @Test
        @DisplayName("Should throw StudentNotFoundException when deleting non-existent student")
        void shouldThrowWhenDeletingNonExistentStudent() {
            // ARRANGE
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            // ACT + ASSERT
            assertThatThrownBy(() -> studentService.deleteStudent(99L))
                    .isInstanceOf(StudentNotFoundException.class);

            verify(studentRepository, never()).delete(any(Student.class));
        }
    }

    // ============================================================
    //  ArgumentCaptor — capture what was passed to mock
    // ============================================================
    @Test
    @DisplayName("Should capture the saved student using ArgumentCaptor")
    void shouldCaptureStudentOnSave() {
        // ARRANGE
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student1);

        // ACT
        studentService.createStudent(student1);

        // CAPTURE — grab what was actually passed to save()
        verify(studentRepository).save(studentCaptor.capture());
        Student captured = studentCaptor.getValue();

        // ASSERT on captured value
        assertThat(captured.getEmail()).isEqualTo("ashish@example.com");
        assertThat(captured.getGrade()).isEqualTo(8);
    }
}
