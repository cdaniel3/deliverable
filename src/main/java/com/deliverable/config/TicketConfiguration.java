package com.deliverable.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TicketConfiguration {

	@Value("${deliverable.config.default.status}")
	private String defaultStatus;
	
	@Value("${deliverable.config.default.priority}")
	private String defaultPriority;

	public String getDefaultStatus() {
		return defaultStatus;
	}

	public void setDefaultStatus(String defaultStatus) {
		this.defaultStatus = defaultStatus;
	}

	public String getDefaultPriority() {
		return defaultPriority;
	}

	public void setDefaultPriority(String defaultPriority) {
		this.defaultPriority = defaultPriority;
	}

	
}
