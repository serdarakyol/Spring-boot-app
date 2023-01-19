package com.example.demo.serviceIml;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.controller.CommonResponses;
import com.example.demo.entity.Teacher;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.service.TeacherService;
import com.example.demo.utils.Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final String teacherNotExistByIdMsg = "Teacher doesn't exist with ID: ";

    @Autowired
    private final TeacherRepository teacherRepository;
    private Utils customUtils = new Utils();
    

    @Override
    public void addNewTeacher(Teacher teacher) {
        Optional<Teacher> teacherOptional = teacherRepository.findTeacherByTeacherEmail(teacher.getTeacherEmail());
        if (teacherOptional.isPresent()) {
            throw new BadRequestException(CommonResponses.emailTakenMsg);
        }

        // check if mail is valid
        if ( !customUtils.isMailValid(teacher.getTeacherEmail()) ) {
            throw new BadRequestException(CommonResponses.emailNotValidMsg);
        }
        teacherRepository.save(teacher);
    }

    @Transactional
    @Override
    public void updateTeacher(int teacherId, String teacherName, String teacherEmail){
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
            () -> new NotFoundException(teacherNotExistByIdMsg + teacherId)
        );

        if (teacherName != null && teacherName.length() > 1 && !Objects.equals(
            teacher.getTeacherName(), teacherName
        )) {
            teacher.setTeacherName(teacherName);
        }

        if (!customUtils.isMailValid(teacherEmail)) {
            throw new BadRequestException(CommonResponses.emailNotValidMsg);
        }

        if (teacherEmail != null && teacherEmail.length() > 1 && !Objects.equals(
            teacher.getTeacherEmail(), teacherEmail
        )) {
            Optional<Teacher> teacherOptional = teacherRepository.findTeacherByTeacherEmail(teacherEmail);
            if (teacherOptional.isPresent()) {
                throw new BadRequestException(CommonResponses.emailTakenMsg);
            }

            teacher.setTeacherEmail(teacherEmail);
        }
        
    }

    @Override
    public List<Teacher> getTeachers() {
        return teacherRepository.findAll();
    }
}
