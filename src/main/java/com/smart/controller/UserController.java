package com.smart.controller;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import com.razorpay.*;
@Controller
@RequestMapping("/user")

public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private MyOrderRepository myOrderRepository;
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m , Principal principal )
	{
		String userName = principal.getName();
		System.out.println(userName);

		// get the user using username(Email)

		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		m.addAttribute("user", user);
		m.addAttribute("title","User Dashboard");
	}
	
	// user dashboard handler
	@RequestMapping("/index")
	public String dashboard(Model m, Principal principal) {
		
		return "normal/user_dashboard";
	}

	// open Add form handler 
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	// proccessing Add Contact form handler
	@PostMapping("/process-contact")
	public String proccessContact(@ModelAttribute Contact contact ,
				@RequestParam("profileImage") MultipartFile file ,
				Principal principal ,HttpSession session )
	{
		try {
			
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			
			if(file.isEmpty())
			{
				 // if file is empty 
				contact.setImage("default.png");
			}
			else
			{
				//upload file to folder and update the name to contact
				
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is Uploaded");
			}
			
			user.getContacts().add(contact);
			contact.setUser(user);
			
			
			
			this.userRepository.save(user);
			System.out.println("Data "+contact);
			
			System.out.println("Added to Database");
			
			// msg success...
				session.setAttribute("message",new Message("Your Contact is Added Successfully", "success"));
			
			
		} catch (Exception e) {
			 System.out.println("Error :"+e.getMessage());
			
			 //error msg....
			 session.setAttribute("message",new Message("Something went wrong try again!", "danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	// per page = 5 [n]
	// current page = 0 [page]
	//using path variable 
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page , Model m , Principal principal)
	{
		m.addAttribute("title","Show User Contacts");
		
		//send the list of contacts !
		 String name = principal.getName();
		  User user = this.userRepository.getUserByUserName(name);
		  
		  Pageable pageable = PageRequest.of(page, 4);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	// showing particular contact detail
	
	@GetMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId")Integer cId,Model m ,Principal principal)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			Contact contact = contactOptional.get();
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			if(user.getId()==contact.getUser().getId())
			{
				m.addAttribute("contact",contact);
				m.addAttribute("title",contact.getName());
			}
			
			
		return"normal/contact_detail";
	}
	
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId")Integer cid ,Model m,Principal principal,HttpSession session)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		//
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		if(user.getId()==contact.getUser().getId())
		{
			contact.setUser(null);
			
			//remove photo
			String image = contact.getImage();
			if (!image.equals("default.png") && !image.isEmpty()) {
	            
	            // Delete the image file
	            try {
	            	File deleteFile = new ClassPathResource("static/img").getFile();
		            String imagePath = deleteFile+File.separator+ image;

	                Files.deleteIfExists(Paths.get(imagePath));
	            } catch (IOException e) {
	                
	            	session.setAttribute("message", new Message("Something went wrong! "+e.getMessage(),"danger"));
	            	return"redirect:/user/show-contacts/0";
	            }
	        }

			
			this.contactRepository.deleteByIdCustom(cid);
			session.setAttribute("message", new Message("Contact deleted successfully","success"));
		}
		else
		{
			session.setAttribute("message", new Message("You dont have permission do to this","danger"));
		}
		
		//redirect
		return"redirect:/user/show-contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId , Model m )
	{
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//processing update form handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact ,
			@RequestParam("profileImage")MultipartFile file ,Model m , HttpSession session ,Principal principal )
	{
		//old Contact detail
		Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
		
		try {
			if(!file.isEmpty())
			{
				//file work --> delete old one ---> save new one

				//update old photo
				 File saveFile = new ClassPathResource("static/img").getFile();
				 Path path = Paths.get(saveFile.getAbsolutePath()+ File.separator + file.getOriginalFilename());
	             Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
	             contact.setImage(file.getOriginalFilename());
	            // delete old image if it is not the default one
	             String image = oldContactDetail.getImage();
	             if (!image.equals("default.png") && !image.isEmpty())
	             {
	            	 	File deleteFile = new ClassPathResource("static/img").getFile();
			            String imagePath = deleteFile+File.separator+ image;

		                Files.deleteIfExists(Paths.get(imagePath));
	             }
	             
			}
			else
			{
				contact.setImage(oldContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is Updated Successfully","success"));
			
		} catch (Exception e) {
			 
		}		
		
		System.out.println(contact.getName());
		return"redirect:/user/contact/"+contact.getcId(); 
	}
	// Your profile handler 
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	// change password handler
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword , 
			@RequestParam("newPassword")String newPassword, Principal principal, HttpSession session)
	{
		System.out.println("old pass "+oldPassword);
		System.out.println("new pass "+newPassword);
		
		User currentUser = this.userRepository.getUserByUserName(principal.getName());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Message("Your Password is successfully changed", "success"));
			
		}
		else
		{
			session.setAttribute("message",new Message("Please Enter Correct Old Password", "danger"));
			return "redirect:/user/settings";

		}
		
		return "redirect:/user/index";
	}
	
	//creating order for payment
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object>data,Principal pricipal) throws Exception
	{
		//System.out.println("order handler executed");
		System.out.println(data);
		
		int amt = Integer.parseInt(data.get("amount").toString());
		String key ="rzp_test_HSiSOAFjV7KT46";
		String secretKey ="4HfgOwAWnaKXlmMBqXipytCm";
		
		 
			RazorpayClient client = new RazorpayClient(key, secretKey);
			
			JSONObject ob = new JSONObject();
			ob.put("amount", amt*100); // raise 
			ob.put("currency", "INR");
			ob.put("receipt", "txn_1234");
			
			//creating new order 
			
			Order order = client.Orders.create(ob);		 
			System.out.println(order);
			
			// you can save this in the database as well !
			
			MyOrder myOrder = new MyOrder();
			myOrder.setAmount(order.get("amount")+"");
			myOrder.setOrderId(order.get("id"));
			myOrder.setStatus("created");
			myOrder.setUser(this.userRepository.getUserByUserName(pricipal.getName()));
			myOrder.setReceipt(order.get("receipt"));
			
			this.myOrderRepository.save(myOrder);
			
			
		return  order.toString();
	}
	
	//update payment detail 
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String , Object> data)
	{
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		
		this.myOrderRepository.save(myOrder);
		
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
}
