package com.grras.testing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grras.testing.repository.StudentRepository;
import com.grras.testing.entity.Student;
import com.grras.testing.exception.DuplicateEmailException;
import com.grras.testing.exception.StudentNotFoundException;

import java.util.List;

/**
 * StudentService — Business Logic Layer
 *
 * Annotations used:
 *  @Service       — marks as Spring service bean
 *  @Transactional — ensures DB operations run in a transaction
 *  @RequiredArgsConstructor — Lombok: constructor injection
 *  @Slf4j         — Lombok: logger
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * Create a new student (throws if email already exists)
     */
    @Transactional
    public Student createStudent(Student student) {
        log.info("Creating student with email: {}", student.getEmail());

        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateEmailException(
                "Student with email " + student.getEmail() + " already exists"
            );
        }

        Student saved = studentRepository.save(student);
        log.info("Student created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get all students
     */
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        log.info("Fetching all students");
        return studentRepository.findAll();
    }

    /**
     * Get student by ID (throws if not found)
     */
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        log.info("Fetching student with ID: {}", id);
        return studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));
    }

    /**
     * Get student by email
     */
    @Transactional(readOnly = true)
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
            .orElseThrow(() -> new StudentNotFoundException("Student not found with email: " + email));
    }

    /**
     * Update existing student
     */
    @Transactional
    public Student updateStudent(Long id, Student updatedStudent) {
        log.info("Updating student with ID: {}", id);

        Student existing = getStudentById(id); // throws if not found

        // Check email conflict only if email is changed
        if (!existing.getEmail().equals(updatedStudent.getEmail())
                && studentRepository.existsByEmail(updatedStudent.getEmail())) {
            throw new DuplicateEmailException(
                "Email " + updatedStudent.getEmail() + " is already taken"
            );
        }

        existing.setName(updatedStudent.getName());
        existing.setEmail(updatedStudent.getEmail());
        existing.setGrade(updatedStudent.getGrade());

        return studentRepository.save(existing);
    }

    /**
     * Delete student by ID
     */
    @Transactional
    public void deleteStudent(Long id) {
        log.info("Deleting student with ID: {}", id);
        Student student = getStudentById(id); // throws if not found
        studentRepository.delete(student);
    }

    /**
     * Search students by name keyword
     */
    @Transactional(readOnly = true)
    public List<Student> searchByName(String keyword) {
        return studentRepository.searchByName(keyword);
    }
}
