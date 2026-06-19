package com.example.m6_thermal_power_plant_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

/**
 * Phòng ban / Phân xưởng.
 * Table: departments
 *
 * Soft delete: cột "is_deleted" do Hibernate tự quản lý.
 * repository.delete(...) sẽ UPDATE is_deleted = true thay vì xoá thật;
 * mọi câu lệnh SELECT tự động bỏ qua các dòng đã xoá.
 */
@Entity
@Table(name = "departments")
@SoftDelete(columnName = "is_deleted", strategy = SoftDeleteType.DELETED)
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "employees")
@EqualsAndHashCode(of = "id")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "department_code", unique = true, nullable = false, length = 50)
    private String departmentCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees;
}
