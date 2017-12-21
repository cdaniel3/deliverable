package tech.corydaniel.web;

public class RestError {

	private long timestamp;
	private String error;
	private int status;
	private String message;
	
	public RestError(long timestamp, String error, int status, String message) {
		this.timestamp = timestamp;
		this.error = error;
		this.status = status;
		this.message = message;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getError() {
		return error;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
}
