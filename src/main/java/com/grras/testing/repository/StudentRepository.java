package com.grras.testing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.grras.testing.entity.Student;

import java.util.List;
import java.util.Optional;

/**
 * StudentRepository — Data access layer
 * Extends JpaRepository for CRUD + pagination support
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Derived Query Method
    Optional<Student> findByEmail(String email);

    // Derived Query — find by grade
    List<Student> findByGrade(Integer grade);

    // Check if email already exists
    boolean existsByEmail(String email);

    // Custom JPQL Query
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:keyword%")
    List<Student> searchByName(@Param("keyword") String keyword);

    // Custom Native Query
    @Query(value = "SELECT * FROM students WHERE grade >= :minGrade", nativeQuery = true)
    List<Student> findStudentsWithMinGrade(@Param("minGrade") Integer minGrade);
}
