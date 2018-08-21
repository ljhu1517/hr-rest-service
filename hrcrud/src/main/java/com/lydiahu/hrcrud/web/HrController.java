package com.lydiahu.hrcrud.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.lydiahu.hrcrud.dto.EmployeeDTO;
import com.lydiahu.hrcrud.model.Department;
import com.lydiahu.hrcrud.model.Employee;
import com.lydiahu.hrcrud.model.Job;
import com.lydiahu.hrcrud.service.DepartmentService;
import com.lydiahu.hrcrud.service.EmployeeService;
import com.lydiahu.hrcrud.service.JobService;

@RestController
public class HrController {

	@Autowired
	private EmployeeService employeeService; 
	
	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private JobService jobService;
	
	public void login() {
		
	}
	
//	@RequestMapping(value = "/")
//	public String load(Model model) {
//		
//		List<Employee> employees = this.employeeService.findAll();
//		List<EmployeeDTO> dtos = new ArrayList<EmployeeDTO>();
//		
//		for(Employee bean: employees) {
//			EmployeeDTO beanDTO =this.getEmployeeDto(bean);
//			dtos.add(beanDTO);
//		}
//		
//		model.addAttribute("dtos", dtos);
//		
//		return "index";
//	}
	
	@CrossOrigin
	@RequestMapping(value = "/getAll", consumes = "application/json")
	public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
		
		List<EmployeeDTO> list = new ArrayList<EmployeeDTO>();
		
		HttpStatus httpStatus = HttpStatus.OK;
		
		try {
			for(Employee e : this.employeeService.findAll()) {
				EmployeeDTO edto = this.getEmployeeDto(e);
				list.add(edto);
			}
		}catch(Exception e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		ResponseEntity<List<EmployeeDTO>> entity = new ResponseEntity(list, httpStatus);
		return entity;
	}
	
//	@RequestMapping(value = "/getOne/{id}")
//	public String getEmployeeById(Model model, @PathVariable("id") BigDecimal id) {
//		
//		Employee e = this.employeeService.findById(id);
//		
//		model.addAttribute("indvEmpKey", e);
//		return null; 
//	}
	

	@PostMapping(value = "/add-new")
	@CrossOrigin
	public ResponseEntity<String> addEmp(HttpEntity<Map<String, String>> httpEntity) {
		
		Map<String, String> params = httpEntity.getBody();
					
		String fname = params.get("fname");
		String lname = params.get("lname");
		String email = params.get("email");
		String salary = params.get("salary");
		String dateString = params.get("hiredate");

		Job job = this.jobService.findById(new BigDecimal(4));	
		Department dept = this.departmentService.findOne(new BigDecimal(5));
		
		try {
			Employee e = new Employee();
		
			e.setFirstName(fname);
			e.setLastName(lname);
			e.setEmail(email);
			e.setDepartment(dept);
			e.setJob(job);
			e.setSalary(new BigDecimal(salary)); 
			e.setHireDate(parseDate(dateString));
			
			this.employeeService.saveOrUpdate(e);
			
		}catch(Exception e){
			return new ResponseEntity<String> ("Fail", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<String> ("Success", HttpStatus.OK);
	}
	
	@PostMapping(value = "/delete", consumes = "application/json")
	@CrossOrigin
	public ResponseEntity<String> deleteEmployee(HttpEntity<Map<String, String>> httpEntity) {
		
		Map<String, String> params = httpEntity.getBody();

		String id = params.get("id");
		
		//Employee e = this.employeeService.findById(new BigDecimal(id));		//access existing employee using ID 
		
		try {
			this.employeeService.delete(new BigDecimal(id));
			
		}catch(Exception exc) {
			return new ResponseEntity<String> ("Fail", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<String> ("Success", HttpStatus.OK);

	}
	
	@PostMapping(value = "/update", consumes = "application/json")
	@CrossOrigin
	public ResponseEntity<String> updateEmployee(HttpEntity<Map<String, String>> httpEntity) {
		
		Map<String, String> params = httpEntity.getBody();
		
		String fname = params.get("fname");
		String lname = params.get("lname");
		String id = params.get("id");
		String email = params.get("email");
		String salary = params.get("salary");
		String dateString = params.get("hiredate");
		//String managerId = params.get("managerId");
		
		Employee e = this.employeeService.findById(new BigDecimal(id));		//access existing employee using ID 
		
		try {
			e.setFirstName(fname);
			e.setLastName(lname);
			e.setEmail(email);
			
			//e.setManagerId((managerId == null || managerId == "") ? null : new BigDecimal(managerId));
			this.employeeService.saveOrUpdate(e);  //business logic 
			
		}catch(Exception exc) {
			return new ResponseEntity<String> ("Fail", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<String> ("Success", HttpStatus.OK);
	}

	private Date parseDate(String value) throws Exception {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate dateTime = LocalDate.parse(value, formatter);
		
		java.util.Date date = java.sql.Date.valueOf(dateTime);
		
		return date;
	}
	
	@CrossOrigin
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/load-update", consumes = "application/json")
	public ResponseEntity<EmployeeDTO> loadUpdatePage(HttpEntity<Map<String, String>> httpEntity) {
		
		Employee e = this.employeeService.findById(new BigDecimal(httpEntity.getBody().get("id")));
		EmployeeDTO dto = this.getEmployeeDto(e);
		
		ResponseEntity<EmployeeDTO> entity = new ResponseEntity(dto, HttpStatus.OK);
		
		return entity;
	}
	
	private EmployeeDTO getEmployeeDto(Employee e) {
		
		EmployeeDTO dto = new EmployeeDTO();
		
		dto.setFirstName(e.getFirstName());
		dto.setLastName(e.getLastName());
		dto.setEmail(e.getEmail());
		dto.setEmployeeId(e.getId());
		dto.setSalary(e.getSalary());
		dto.setEmployeeId(e.getId());
		//dto.setDepartmentName(e.getDepartment().getName());
		
		Employee manager = (e.getManagerId() == null ? null : this.employeeService.findById(e.getManagerId()));
		dto.setManagerName(manager == null ? "" : manager.toString());
		dto.setEditUrl("<a href='/load-update?id=" + dto.getEmployeeId()+ "'");
		dto.setDeleteUrl("<a href='/delete?id=" + dto.getEmployeeId() + "'");
		return dto;
	}
	
//	private List<EmployeeDTO> getManagers() {
//		
//		List<Employee> all = this.employeeService.findAll();
//		//List<Employee> managers0 = all.stream()
//			//	.filter(bean -> (bean.getJob().getId().equals(new BigDecimal(2)) || bean.getJob().getId().equals(new BigDecimal(3))))
//				//.collect(Collectors.toList());
//		
//		List<EmployeeDTO> managers = new ArrayList<EmployeeDTO>();
//		
//		for(Employee emp : managers0) {
//			EmployeeDTO bean = new EmployeeDTO();
//			//bean.setDepartmentName(emp.getDepartment().getName());
//			bean.setFirstName(emp.getFirstName());
//			bean.setLastName(emp.getLastName());
//			bean.setEmail(emp.getEmail());
//			bean.setEmployeeId(emp.getId());
//			bean.setManagerName(emp.toString());
//			managers.add(bean);
//		}
//		
//		return managers;
//	}
	
}
