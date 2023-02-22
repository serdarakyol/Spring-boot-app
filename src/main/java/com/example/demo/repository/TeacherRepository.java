package com.example.demo.repository;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Teacher;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    @Query("SELECT t FROM Teacher t WHERE t.teacherEmail = ?1")
    Optional<Teacher> findTeacherByTeacherEmail(String teacherEmail);

    @Modifying
    @Transactional
    @Query("DELETE FROM Teacher t WHERE t.teacherEmail = :teacherEmail")
    void deleteTeacherByMail(String teacherEmail);
}
