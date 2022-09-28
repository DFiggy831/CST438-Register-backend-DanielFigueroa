package com.cst438.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.CourseRepository;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.ScheduleDTO;
import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.cst438.domain.StudentRepository;
import com.cst438.service.GradebookService;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://registerf-cst438.herokuapp.com/"})
public class StudentController {
	
	//TODO finish this so it admin can add students to the "system"
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	StudentRepository studentRepository;
	
	@Autowired
	EnrollmentRepository enrollmentRepository;
	
	@Autowired
	GradebookService gradebookService;
	
	
	@PostMapping("/student")
	@Transactional
	public StudentDTO addStudent( @RequestBody StudentDTO sDTO) {
		StudentDTO student;
		Student currentStudent = studentRepository.findByEmail(sDTO.email);
		if (currentStudent == null) {
			// create new student object
			Student tempStudent = new Student();
			tempStudent.setName(sDTO.name);
			tempStudent.setEmail(sDTO.email);
			
			// store student within repository 
			tempStudent = studentRepository.save(tempStudent);
			student = createStudentDTO(tempStudent);
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student with this email already exists: " + sDTO.email);
		}
		return student;
	}
	
	@PutMapping("/student/hold/{id}")
	public void placeHold( @PathVariable("id") int sid) {
		Student currentStudent = studentRepository.findById(sid).orElse(null);
		
		// student with bad ID appears 
		if (currentStudent == null) {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student with this ID does not exist: " + sid);
		} else if (currentStudent.getStatusCode() != 1){ // if they do not have a hold
			currentStudent.setStatusCode(1);
			currentStudent = studentRepository.save(currentStudent);
			System.out.print("SUCCESSFULLY PLACED HOLD");
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student with this ID: " + sid + " already has a hold.");
		}
	}
	
	// complete method for releasing HOLD to student registration
	@PutMapping("/student/release/{id}")
	public void releaseHold( @PathVariable("id") int sid) {
		Student currentStudent = studentRepository.findById(sid).orElse(null);
		
		// student with bad ID appears 
		if (currentStudent == null) {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student with this ID does not exist: " + sid);
		} else if (currentStudent.getStatusCode() != 0){
			currentStudent.setStatusCode(0);
			currentStudent = studentRepository.save(currentStudent);
			System.out.print("SUCCESSFULLY PLACED HOLD");
		} else {
			throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "Student with this ID: " + sid + " does not have a hold.");
		}
	}
	
	
	
	
	private StudentDTO createStudentDTO(Student student) {
		StudentDTO sDTO = new StudentDTO();
		
		sDTO.student_id = student.getStudent_id();
		sDTO.email = student.getEmail();
		sDTO.name = student.getName();
		sDTO.status = student.getStatus();
		sDTO.statusCode = student.getStatusCode();
		
		return sDTO;
	}
	
	
}
