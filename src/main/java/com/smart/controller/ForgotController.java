package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	//email id form open handler 
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email , HttpSession session )
	{
		System.out.println(email);
		 
		// check if user exists or not !
		
		User user = this.userRepository.getUserByUserName(email);
		
		if(user==null)
		{
			//send error message
			session.setAttribute("message","NO USER IS REGISTERED WITH THIS EMAIL!! please check the email entered and try again!");
			return "forgot_email_form";
		}
		
		
		
		//generating Otp of 4 digit
		
		
		int otp = random.nextInt(99999);
		System.out.println("OTP :"+otp);
		
		// code to send otp :)
		
		String subject="OTP for verification from Smart-Contact-Manager";
		String message="<div style='border : 1px solid #e2e2e2 ;padding:20px'>"
						+"<h1>"
						+"OTP is "
						+"<b>"
						+otp
						+"</b>"
						+"</h2>"
						+"<div>" ;
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message,to);
		System.out.println(flag);
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email",email);
			return "verify_otp";
		}
		session.setAttribute("message","Check your email id !!");
		return "forgot_email_form";
	}
	
	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp")Integer otp , HttpSession session )
	{
		Integer myOtp =  (Integer)session.getAttribute("myotp");
		
		
		System.out.println("otp in session"+myOtp);
		System.out.println("otp user entered "+otp);
		
		if(myOtp.equals(otp))
		{
			// password change form
			
			System.out.println("conditions matched!");
			 
			return "password_change_form";
		}
		else
		{
			session.setAttribute("message","You have entered wrong otp !");
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changepassword(@RequestParam("newpassword")String newpassword , HttpSession session)
	{
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		 
		this.userRepository.save(user);
		
		
		
		return "redirect:/signin?change=Password Changed Successfully";
	}
}
