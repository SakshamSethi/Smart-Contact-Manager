package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepo;
	
	//home page handler 
	
	@RequestMapping("/")
	public String home(Model m)
	{
		m.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model m)
	{
		m.addAttribute("title","About- Smart Contact Manager");
		return "about";
	}
	
	//Register Handler
	@RequestMapping("/signup")
	public String signup(Model m)
	{
		m.addAttribute("title","Register- Smart Contact Manager");
		m.addAttribute("user",new User());
		return "signup";
	}
	
	// Handler for Registering user
	
	
	@PostMapping("/do_register")
	
	public String registerUser(@Valid @ModelAttribute("user")User user , BindingResult result1,
			@RequestParam(value="agreement",defaultValue ="false" )boolean agreement ,
			Model m  )
	{
		
		try {
			
			if(!agreement)
			{
				System.out.println("You have not agreed to terms and conditions");
				throw new Exception("You have not agreed to terms and conditions");
				
			}
			
			if(result1.hasErrors())
				{
				System.out.println("Error"+result1);
				m.addAttribute("user",user);
				return "signup";
				}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			System.out.println("password : "+user.getPassword());
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			 
			 
			System.out.println("user"+user);
			
			User result = this.userRepo.save(user);
			 
			m.addAttribute("user",new User());
			m.addAttribute("message", new Message("Registered Successfully","alert-success"));
			
			System.out.println(result);
			
			return "signup";
			
		} catch (Exception e) {
			 
			m.addAttribute("user",user);
			e.printStackTrace();
			m.addAttribute("message", new Message("Some thing went wrong !"+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
		 
	}
	
	// handler for Custom Login 
	@GetMapping("/signin")
	public String customLogin(Model m)
	{
		m.addAttribute("title","Login Page -Smart Contact Manager");
		return "login";
	}
}
