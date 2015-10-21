package com.bs.lock.exception;
/**
 * 异常父类
 * @author sere
 *
 */
public class LockException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LockException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LockException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public LockException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public LockException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LockException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
}
