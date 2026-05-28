package com.fitness.service;

import com.fitness.entity.Notification;
import com.fitness.entity.SystemUser;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class NotificationEvent extends ApplicationEvent {

	private final SystemUser user;
	private final Notification.NotifType type;
	private final String templateName; // Maps to EmailTemplate or defines generic title
	private final Map<String, Object> variables;
	private final String deepLink;
	private final String fallbackTitle;
	private final String fallbackBody;

	public NotificationEvent(Object source, SystemUser user, Notification.NotifType type, 
							 String templateName, Map<String, Object> variables, 
							 String deepLink, String fallbackTitle, String fallbackBody) {
		super(source);
		this.user = user;
		this.type = type;
		this.templateName = templateName;
		this.variables = variables;
		this.deepLink = deepLink;
		this.fallbackTitle = fallbackTitle;
		this.fallbackBody = fallbackBody;
	}
}
