package pl.com.ids;

public enum ProcessingStatus {
	WORKING("WORKING"),
	DONE("DONE");
	
	private String value;
	
	private ProcessingStatus(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
