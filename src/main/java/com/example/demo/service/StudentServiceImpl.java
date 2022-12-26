package com.example.demo.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Student;
import com.example.demo.repository.StudentRepository;
import com.example.demo.utils.EmailValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService{

	private final StudentRepository studentRepository;

    private EmailValidator emailValidator = new EmailValidator();

	/*
	Note to me!!!
	@RequiredArgsConstructor does the same thing like below code
	@Autowired
	public StudentService(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}
	*/
	@Override
	public List<Student> getStudents() {
		return studentRepository.findAll();
	}

	@Override
	public Student getStudentById(Long studentId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new IllegalStateException("Student with ID " + studentId + " doesn't exist"));
		return student;
	}

	@Override
	public void addNewStudent(Student student) {
		Optional<Student> studentOptional = studentRepository.findStudentByEmail(student.getEmail());
		if (studentOptional.isPresent()) {
			throw new IllegalStateException("E-mail is taken. Please add another E-mail");
		}

		// check if mail is valid
		boolean isMailValid = emailValidator.isMailValid(student.getEmail());
		if (!isMailValid){
			throw new IllegalStateException("The E-mail is not valid. Please write a valid e-mail");
		}
		studentRepository.save(student);
		System.out.println(student);
	}

	@Override
	public void deleteStudent(Long studentId) {
		boolean isExist = studentRepository.existsById(studentId);
		if (!isExist) {
			throw new IllegalStateException("Student with ID " + studentId + " doesn't exist");
		}
		studentRepository.deleteById(studentId);
	}

	@Transactional
	@Override
	public void updateStudent(Long studentId, String name, String email) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new IllegalStateException("Student with ID " + studentId + " doesn't exist"));

		if (name != null && name.length() > 0 && !Objects.equals(student.getName(), name)) {
			student.setName(name);
		}

		if (!emailValidator.isMailValid(email)) {
			throw new IllegalStateException("E-mail is not valid");
		}

		if (email != null && email.length() > 0 && !Objects.equals(student.getEmail(), email)) {
			Optional<Student> studentOptional = studentRepository.findStudentByEmail(email);
			// check if e-mail taken
			if (studentOptional.isPresent()) {
				throw new IllegalStateException("E-mail taken");
			}
			student.setEmail(email);
		}
	}

}