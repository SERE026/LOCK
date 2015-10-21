package com.bs.lock.exception;
/**
 * 当节点出现不存在时抛出此异常
 * @author sere
 *
 */
public class NodeNotExistsException extends LockException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NodeNotExistsException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NodeNotExistsException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public NodeNotExistsException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NodeNotExistsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NodeNotExistsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	

}
