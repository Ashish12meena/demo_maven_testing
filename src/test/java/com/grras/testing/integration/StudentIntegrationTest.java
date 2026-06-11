package com.grras.testing.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grras.testing.entity.Student;
import com.grras.testing.repository.StudentRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

/**
 * ============================================================
 *  INTEGRATION TESTS — Full Stack (Controller → Service → Repository → MySQL)
 * ============================================================
 *
 *  @SpringBootTest
 *      → Loads the FULL Spring Application Context.
 *        All beans (@Controller, @Service, @Repository) are loaded.
 *        Uses MySQL DB configured in application.properties.
 *        This is the closest to running the real application.
 *
 *  @AutoConfigureMockMvc
 *      → Injects MockMvc automatically. Allows sending fake HTTP requests
 *        without starting an actual HTTP server (Tomcat not started).
 *
 *  @BeforeEach + @AfterEach (deleteAll)
 *      → Since we are using real MySQL (not in-memory), we manually
 *        clean up data before and after each test to keep tests isolated.
 *        This replaces the @Transactional + @Rollback approach.
 *
 *  @TestMethodOrder
 *      → Controls the order in which test methods run.
 * ============================================================
 */
@SpringBootTest                                         // ← Full Spring context + MySQL
@AutoConfigureMockMvc                                   // ← Auto-injects MockMvc
@TestMethodOrder(MethodOrderer.DisplayName.class)       // ← Run in display name order
@DisplayName("Student Integration Tests")
class StudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;                            // ← Simulates HTTP requests

    @Autowired
    private StudentRepository studentRepository;        // ← Direct DB access for setup/assert

    @Autowired
    private ObjectMapper objectMapper;                  // ← JSON serialization

    private Student savedStudent;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();                  // ← Clean MySQL before each test

        savedStudent = studentRepository.save(
            Student.builder()
                .name("Ashish Sharma")
                .email("ashish@example.com")
                .grade(8)
                .build()
        );
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();                  // ← Clean MySQL after each test
    }

    // ============================================================
    //  POST /api/students — Create Student
    // ============================================================

    @Test
    @DisplayName("POST /api/students - should create student and return 201")
    void shouldCreateStudentAndReturn201() throws Exception {
        Student newStudent = Student.builder()
                .name("Rahul Verma")
                .email("rahul@example.com")
                .grade(7)
                .build();

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated())                    // HTTP 201
                .andExpect(jsonPath("$.id").exists())               // id is generated
                .andExpect(jsonPath("$.name").value("Rahul Verma"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"));
    }

    @Test
    @DisplayName("POST /api/students - should return 409 when email already exists")
    void shouldReturn409WhenEmailDuplicate() throws Exception {
        // ashish@example.com already saved in @BeforeEach
        Student duplicate = Student.builder()
                .name("Another Ashish")
                .email("ashish@example.com")            // same email
                .grade(5)
                .build();

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());                  // HTTP 409
    }

    @Test
    @DisplayName("POST /api/students - should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        Student invalidStudent = Student.builder()
                .name("")                               // blank name → @NotBlank fails
                .email("not-an-email")                  // invalid email → @Email fails
                .grade(15)                              // > 10 → @Max fails
                .build();

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudent)))
                .andExpect(status().isBadRequest());                // HTTP 400
    }

    // ============================================================
    //  GET /api/students — Get All
    // ============================================================

    @Test
    @DisplayName("GET /api/students - should return list of students")
    void shouldReturnAllStudents() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())                         // HTTP 200
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].email", notNullValue()));
    }

    // ============================================================
    //  GET /api/students/{id} — Get By ID
    // ============================================================

    @Test
    @DisplayName("GET /api/students/{id} - should return student when found")
    void shouldReturnStudentById() throws Exception {
        mockMvc.perform(get("/api/students/{id}", savedStudent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ashish Sharma"))
                .andExpect(jsonPath("$.email").value("ashish@example.com"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - should return 404 when not found")
    void shouldReturn404WhenStudentNotFound() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 9999L))
                .andExpect(status().isNotFound());                  // HTTP 404
    }

    // ============================================================
    //  PUT /api/students/{id} — Update
    // ============================================================

    @Test
    @DisplayName("PUT /api/students/{id} - should update student successfully")
    void shouldUpdateStudentSuccessfully() throws Exception {
        Student updated = Student.builder()
                .name("Ashish Kumar")                   // name changed
                .email("ashish@example.com")            // same email
                .grade(10)                              // grade updated
                .build();

        mockMvc.perform(put("/api/students/{id}", savedStudent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ashish Kumar"))
                .andExpect(jsonPath("$.grade").value(10));
    }

    // ============================================================
    //  DELETE /api/students/{id} — Delete
    // ============================================================

    @Test
    @DisplayName("DELETE /api/students/{id} - should delete student and return 204")
    void shouldDeleteStudentAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", savedStudent.getId()))
                .andExpect(status().isNoContent());                 // HTTP 204

        // Verify directly in MySQL that student is gone
        assertThat(studentRepository.findById(savedStudent.getId())).isEmpty();
    }

    // ============================================================
    //  GET /api/students/search?keyword= — Search
    // ============================================================

    @Test
    @DisplayName("GET /api/students/search - should return matching students")
    void shouldSearchStudentsByKeyword() throws Exception {
        mockMvc.perform(get("/api/students/search")
                        .param("keyword", "Ashish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsString("Ashish")));
    }
}