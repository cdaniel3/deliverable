package tech.corydaniel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="ticket.config")
public class TicketConfiguration {

	private String defaultStatus;
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
