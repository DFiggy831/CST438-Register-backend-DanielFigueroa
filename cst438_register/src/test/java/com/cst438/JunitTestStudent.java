package com.cst438;

import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cst438.controller.StudentController;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Student;
import com.cst438.domain.StudentDTO;
import com.cst438.domain.StudentRepository;
import com.cst438.service.GradebookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ContextConfiguration;

/* 
 * Example of using Junit with Mockito for mock objects
 *  the database repositories are mocked with test data.
 *  
 * Mockmvc is used to test a simulated REST call to the RestController
 * 
 * the http response and repository is verified.
 * 
 *   Note: This tests uses Junit 5.
 *  ContextConfiguration identifies the controller class to be tested
 *  addFilters=false turns off security.  (I could not get security to work in test environment.)
 *  WebMvcTest is needed for test environment to create Repository classes.
 */
@ContextConfiguration(classes = { StudentController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestStudent {

	static final String URL = "http://localhost:8080";
	
	public static final String TEST_STUDENT_EMAIL = "d@";
	public static final String TEST_STUDENT_NAME  = "daniel";
	public static final int TEST_STUDENT_ID = 3;
	
	

	@MockBean
	CourseRepository courseRepository;

	@MockBean
	StudentRepository studentRepository;

	@MockBean
	EnrollmentRepository enrollmentRepository;

	@MockBean
	GradebookService gradebookService;

	@Autowired
	private MockMvc mvc;


	
	
	@Test
	public void addStudent() throws Exception {
		
		MockHttpServletResponse response;

		Student student = new Student();
		
		student.setStudent_id(TEST_STUDENT_ID);
		student.setName(TEST_STUDENT_NAME);
		student.setEmail(TEST_STUDENT_EMAIL);
		
		// student controller process
		// create studentDTO object 
		// find and verify that email does not already exist
		// create new student object
		// set new name and email
		// store student within repository
		// return student
		
		given(studentRepository.save(any(Student.class))).willReturn(student);
		
		
		StudentDTO sDTO = new StudentDTO();
		sDTO.name = TEST_STUDENT_NAME; 
		sDTO.email = TEST_STUDENT_EMAIL;
		
		
		response = mvc.perform(
				MockMvcRequestBuilders
			      .post("/student")
			      .content(asJsonString(sDTO))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// Make sure it succeeds
		assertEquals(200, response.getStatus());
		
		// valid primary key
		StudentDTO verified = fromJsonString(response.getContentAsString(), StudentDTO.class);
		assertNotEquals(0, verified.student_id);
				
		// Make sure save was used
		verify(studentRepository).save(any(Student.class));
		
	}
	
	@Test
	public void placeHold() throws Exception {
		MockHttpServletResponse response;
		
		boolean hold;

		Student student = new Student();
		
		student.setStudent_id(TEST_STUDENT_ID);
		student.setName(TEST_STUDENT_NAME);
		student.setStatus(null);
		
		// student controller process
		// find student by id
		// if student code is not 1, set status code to 1
		// save to repository
		
		
		given(studentRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.of(student));
		given(studentRepository.save(any(Student.class))).willReturn(student);
		
		StudentDTO sDTO = new StudentDTO();
		sDTO.student_id = TEST_STUDENT_ID;
		sDTO.name = TEST_STUDENT_NAME; 
		
		
		response = mvc.perform(
				MockMvcRequestBuilders
			      .put("/student/hold/"+TEST_STUDENT_ID)
			      .content(asJsonString(sDTO))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// Make sure it succeeds
		assertEquals(200, response.getStatus());
		
		// make sure findbyId was called 
		verify(studentRepository, times(1)).findById(TEST_STUDENT_ID);
		
		// verify status code
		if (student.getStatusCode() != 0) {
			hold = true;
			assertEquals(true, hold, "Success!");
		} else {
			hold = false;
			assertEquals(true, hold, "Failure!");
		}
		
	}
	
	@Test
	public void releaseHold() throws Exception {
		MockHttpServletResponse response;
		
		boolean hold;

		Student student = new Student();
		
		student.setStudent_id(TEST_STUDENT_ID);
		student.setName(TEST_STUDENT_NAME);
		student.setStatus(null);
		student.setStatusCode(1);
		
		// student controller process
		// find student by id
		// if student code is not 0, set status code to 0
		// save to repository
		
		given(studentRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.of(student));
		given(studentRepository.save(any(Student.class))).willReturn(student);
		
		StudentDTO sDTO = new StudentDTO();
		sDTO.student_id = TEST_STUDENT_ID;
		sDTO.name = TEST_STUDENT_NAME; 
		
		
		response = mvc.perform(
				MockMvcRequestBuilders
			      .put("/student/release/"+TEST_STUDENT_ID)
			      .content(asJsonString(sDTO))
			      .contentType(MediaType.APPLICATION_JSON)
			      .accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// Make sure it succeeds
		assertEquals(200, response.getStatus());
		// make sure findbyId was called 
		verify(studentRepository, times(1)).findById(TEST_STUDENT_ID);
		// verify status code
		if (student.getStatusCode() != 0) {
			hold = false;
			assertEquals(true, hold, "Failure!");
		} else {
			hold = true;
			assertEquals(true, hold, "Success!");
		}
				
	}

	private static String asJsonString(final Object obj) {
		try {

			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T  fromJsonString(String str, Class<T> valueType ) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
