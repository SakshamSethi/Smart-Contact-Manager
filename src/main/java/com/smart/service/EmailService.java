package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {

		boolean f = false;
		String from = "sethisaksham27@gmail.com";

		// same code as done in eclipse
		String host = "smtp.gmail.com";

		// get the system properties
		Properties properties = System.getProperties();

		System.out.println("Properties :" + properties);

		// setting important infomation to properties object

		// host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
        // properties.put("mail.smtp.ssl.trust", "*");

		// step 1 : get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("sethisaksham27@gmail.com", "ysxn gkiu wnfb uust");
			}

		});

		// step 2 : compose the message
		session.setDebug(true);
		MimeMessage mimeMessage = new MimeMessage(session);

		try {
			// from email
			mimeMessage.setFrom(from);

			// recipient email
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject
			mimeMessage.setSubject(subject);

			// adding text to msg
			//mimeMessage.setText(message);
			  mimeMessage.setContent(message,"text/html");	
			// step 3 : send using Transport class
			Transport.send(mimeMessage);
			System.out.println("Send Successfully");
			f=true;

		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return f;
	}
}
