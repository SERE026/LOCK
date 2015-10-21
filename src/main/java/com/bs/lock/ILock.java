package com.bs.lock;
/**
 * 锁操作接口
 * @author sere
 *
 */
public interface ILock {
	
	/**
	 * 获取锁
	 */
	public void lock();
	
	/**
	 * 释放锁
	 */
	public void unlock();
	
	/**
	 * 创建一个与服务器的连接
	 */
	public void connect();
	
	/**
	 *  查询是否被锁住
	 * @return
	 */
	public boolean isLock();
	
	/**
	 * 获取锁，如果是最小节点 返回true 否则返回false
	 * @return
	 */
	public boolean tryLock();
}
