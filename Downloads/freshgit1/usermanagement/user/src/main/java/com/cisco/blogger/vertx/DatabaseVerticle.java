package com.cisco.blogger.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.cisco.blogger.dao.UserDao;
import com.cisco.blogger.model.Blog;
import com.cisco.blogger.model.BlogComment;
import com.cisco.blogger.model.User;

public class DatabaseVerticle extends AbstractVerticle {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	UserDao userDao;
	
	Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	JsonObject config;
	JWTAuth provider;
	
	ApplicationContext context;
	
	public DatabaseVerticle(ApplicationContext context) {
		userDao = (UserDao) context.getBean("userDao");
	}
	
	@Override
	public void start() throws Exception {
		config = new JsonObject().put("keyStore", new JsonObject()
			    .put("path", "keystore.jceks")
			    .put("type", "jceks")
			    .put("password", "secret"));
		 provider = JWTAuth.create(vertx, config);
		 
		vertx.eventBus().consumer("com.cisco.blogger.user.register", message -> {
			User userObj = Json.decodeValue(message.body().toString(), User.class);
			
			Set<ConstraintViolation<User>> constraintViolations = validator.validate(userObj);
			StringBuffer str = new StringBuffer();
			for(ConstraintViolation constraint : constraintViolations){
				if(str.length() != 0){
					str.append(", ");
				}
				str.append(String.format("%s", constraint.getMessage()));
			}
		    logger.info("messages "+str.toString());
		    
		    boolean isSuccess = false; 
		    // Valid registration details
		    if(constraintViolations.size()==0) {
				isSuccess = userDao.createUser(userObj);
				message.reply(isSuccess);
		    } else {
		    	logger.info("Input is not valid");
		    	message.reply(str.toString());
		    }
		    
			
		});
		
		vertx.eventBus().consumer("com.cisco.blogger.user.info.update", message -> {
	    	User user = Json.decodeValue(message.body().toString(), User.class);
	    	Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
			StringBuffer str = new StringBuffer();
			for(ConstraintViolation constraint : constraintViolations){
				if(str.length() != 0){
					str.append(", ");
				}
				str.append(String.format("%s", constraint.getMessage()));
			}
		    logger.info("messages "+str.toString());
		    
		    // Valid registration details
		    if(constraintViolations.size()==0) {
		    	userDao.updateUser(user);
				message.reply(true);
		    } else {
		    	logger.info("Input is not valid");
		    	message.reply(str.toString());
		    }
			
		});
		
		vertx.eventBus().consumer("com.cisco.blogger.user.get", message -> {
			String userName = message.body().toString();
			Optional<User> userOptional = userDao.getUserById(userName);
			User user = null;
			if(userOptional.isPresent()) {
				user = userOptional.get();
			}
			message.reply(Json.encodePrettily(user));
		});
		
		vertx.eventBus().consumer("com.cisco.blogger.user.list",message -> {
			List<User> users = userDao.getAllUsers();
			message.reply(Json.encodePrettily(users));
		});
		
	}
	
}
