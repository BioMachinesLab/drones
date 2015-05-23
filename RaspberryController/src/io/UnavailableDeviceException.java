package io;

public class UnavailableDeviceException extends Exception {
	private static final long serialVersionUID = -4106080742980652030L;

	public UnavailableDeviceException() {
		super();
	}

	public UnavailableDeviceException(String message) {
		super(message);
	}
}
