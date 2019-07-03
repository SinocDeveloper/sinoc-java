package org.sinoc.shell.model.dto;

public class MethodCallDTO {

    private final String methodName;

    private final Long count;

    private final Long lastTime;

    private final String lastResult;

    private final String curl;

	public MethodCallDTO(String methodName, Long count, Long lastTime, String lastResult, String curl) {
		super();
		this.methodName = methodName;
		this.count = count;
		this.lastTime = lastTime;
		this.lastResult = lastResult;
		this.curl = curl;
	}

	public String getMethodName() {
		return methodName;
	}

	public Long getCount() {
		return count;
	}

	public Long getLastTime() {
		return lastTime;
	}

	public String getLastResult() {
		return lastResult;
	}

	public String getCurl() {
		return curl;
	}
}
