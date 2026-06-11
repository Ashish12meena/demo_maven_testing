package com.grras.testing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(name = "students")
@Data                   
@NoArgsConstructor     
@AllArgsConstructor     
@Builder                
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull(message = "Grade cannot be null")
    @Min(value = 1, message = "Grade must be at least 1")
    @Max(value = 10, message = "Grade cannot exceed 10")
    @Column(nullable = false)
    private Integer grade;
}
