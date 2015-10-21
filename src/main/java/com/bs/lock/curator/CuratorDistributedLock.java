package com.bs.lock.curator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lock.curLock.MutexJob;
import lock.curLock.ParallelJob;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bs.lock.ILock;
import com.bs.lock.exception.LockException;
import com.google.common.collect.Lists;
import com.sun.security.sasl.ClientFactoryImpl;

/**
 * 锁实现类 可以重入 相对于不可重入性能上有优势
 * @author sere
 *
 */
public class CuratorDistributedLock implements ILock {

	private static final Logger LOG = LoggerFactory.getLogger(CuratorDistributedLock.class);
    
	/**
	 * zookeeper 节点配置
	 */
    private static final String CONFIG = "192.168.1.222:2181,192.168.1.222:2182,192.168.1.222:2183";
    
    private static CuratorFramework client = CuratorFrameworkFactory.newClient(CONFIG, new ExponentialBackoffRetry(1000, Integer.MAX_VALUE)) ;
   
    private static final String PATH = "/locks";   //节点路径
    
    private static final long WAIT_TIME = 2000; //等待前一节点时间
    
    private InterProcessMutex lock = new InterProcessMutex(client, PATH);  
  
  
    public CuratorDistributedLock(){
    	super();
    	connect();
    }
    
    /*public CuratorDistributedLock(InterProcessLock lock){
    	this.lock = lock;
    	client.start();
    }*/
    

	@Override
	public void lock() {
		try {
			lock.acquire();
		} catch (Exception e) {
			throw new LockException("get lock exception ！", e);
		}
	}

	@Override
	public void unlock() {
		try {
			lock.release();
		} catch (Exception e) {
			lock = null;
			throw new LockException("release lock exception !", e);
		}
	}

	@Override
	public void connect() {
		client.start();
	}

	@Override
	public boolean isLock() {
		return tryLock();
	}

	@Override
	public boolean tryLock() {
		try {
			return lock.acquire(WAIT_TIME,TimeUnit.MICROSECONDS);
		} catch (Exception e) {
			throw new LockException("get lock exception ！", e);
		}
	}  

}
