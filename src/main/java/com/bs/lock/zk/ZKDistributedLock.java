package com.bs.lock.zk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bs.lock.ILock;
import com.bs.lock.exception.ConnectException;
import com.bs.lock.exception.InitialException;
import com.bs.lock.exception.LockException;
import com.bs.lock.exception.NodeNotExistsException;

/**
 * 锁实现类
 * @author sere
 *
 */
public class ZKDistributedLock implements ILock {

	private static final Logger LOG = LoggerFactory.getLogger(ZKDistributedLock.class);
    
	private static final String SEPERATE = "###"; //构造特殊节点是的分割标识
	
    private static final String CONFIG = "192.168.1.222:2181,192.168.1.222:2182,192.168.1.222:2183";
    
    private ZooKeeper zk = null;   //zk 客户端
    private String root = "/locks";//根节点目录
    private String lockName;//竞争资源的标志（由外部程序定义,死锁时重连）
    private String waitNode;//等待前一个锁
    private String thisNode;//当前锁
    private CountDownLatch latch = new CountDownLatch(1);//节点通知计数器
    
    private CountDownLatch conLatch = new CountDownLatch(1);//获取连接通知计数器
    
    private static final int SESSION_TIME = 2000; // zk 会话时间
    private static final long WAIT_TIME = 2000; //等待前一节点时间
    
    private boolean isLock = false;
    
//    private List<Exception> exception = new ArrayList<Exception>(); //异常信息队列
	
	public ZKDistributedLock() {
		super();
	}
	
    /**
	  * 1.创建一个与服务器的连接                                  
	  * 2.创建分布式锁的根节点
	  *    使用前请确认config配置的zookeeper服务可用          
	  * @param config 127.0.0.1:2181,127.0.0.2:2181  
	  *    
	  */
    public ZKDistributedLock(String lockName){
    	this.lockName = lockName;
    	try {
    		connect();
			Stat stat = zk.exists(root, false);
			
			if(stat == null){
				// 创建根节点
				zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 
			}
		} catch (KeeperException e) {
			throw new InitialException("create root fail! ",e);
		} catch (InterruptedException e) {
			throw new InitialException("create root has been interrupted! ",e);
		}
    }

    /**
     * 连接zookeeper 服务器
     */
    public void connect(){
    	try {
			zk = new ZooKeeper(CONFIG, SESSION_TIME, new Watcher(){
				
				/*
				 * zookeeper 节点监视器
				 * (non-Javadoc)
				 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
				 */
				@Override
				public void process(WatchedEvent event) {
					
					Event.KeeperState keeperState = event.getState();
					
					if (Event.KeeperState.SyncConnected == keeperState) {
						LOG.info( "成功连接上ZK服务器");
						conLatch.countDown();
					}
				}
				
			});
			conLatch.await();
		} catch (IOException e) {
			throw new ConnectException(" the lock has not write operation system file! ",e);
		} catch (InterruptedException e) {
			throw new ConnectException("the lock has  been Interrupted! ",e);
		}
    }
    
	
	/** 
	 * 获取锁
	 */
	@Override
	public synchronized void lock() {

		if (zk == null)
			connect();

		boolean f = checkMinPath();

		isLock = f;

	}
	
	/**
	 * 释放锁
	 */
	@Override
	public synchronized void unlock(){
		try {
			if(zk!=null){
				if(zk.exists(thisNode, false) != null) zk.delete(thisNode, -1);
				//zk.close();
			}
		} catch (InterruptedException | KeeperException e) {
			LOG.error("zookeeper 关闭异常"+e.getMessage());
			throw new LockException("delete node exception ",e);
		}
	}
	
	/**
	 * 检查当前节点是否是最小节点
	 * 
	 * 1.检查当前节点是否是最小节点  
	 *  1.1 是最小节点 则拿到锁
	 *  1.2 不是最小节点等待
	 * @return
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	private boolean checkMinPath(){
		//创建特殊标识的锁  以便在server和client 发生异常时 对 幽灵节点的处理
		try {
			thisNode = zk.create(root + "/" + lockName + SEPERATE, null,
					ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			LOG.info("创建锁路径:" + thisNode);
		} catch (KeeperException | InterruptedException e) {
			// 异常时处理，防止 幽灵节点的出现
			try {
				if (zk.exists(thisNode, false) != null) 
					this.latch.wait();
			} catch (InterruptedException | KeeperException e1) {
				e1.printStackTrace();
			}
		}
		
		List<String> lockObjNodes = getSubNodes();
        
        LOG.info(thisNode + "==" + thisNode.substring(root.length() + 1));
        
        //判断是否是最小节点
        int index = lockObjNodes.indexOf(thisNode.substring(root.length() + 1));
        
        switch (index) {
		case -1: {
			LOG.warn( "this node not exsit after create" + thisNode);
			checkMinPath();
			//throw new LockException("this node not exsit after create");
		}
		case 0: {
			LOG.info("本节点是最小节点，拿到锁...." + thisNode);
			return true;
		}
		default: {
			this.waitNode = root + "/" + lockObjNodes.get(index - 1);
			LOG.info( "获取子节点中，排在我前面的" + waitNode);
			return waitLock(waitNode,WAIT_TIME,TimeUnit.MILLISECONDS);
		}
		
        }
	}
	
	/**
	 * 判断比自己小一个数的节点是否存在,
	 * 		1.如果存在则等待锁,同时注册监听
	 * 		2.如果不存在，继续检查当前节点是否是最小节点
	 * @param prevNode 当前节点的前一个节点
	 * @param waitTime 等待时间
	 * 
	 */
	private boolean waitLock(String prevNode, long waitTime,TimeUnit unit) {
		
		try {
			// 判断比自己小一个数的节点是否存在,如果存在则等待锁,同时注册监听
			Stat stat = zk.exists(prevNode, new Watcher(){

				@Override
				public void process(WatchedEvent event) {
					
					Event.KeeperState keeperState = event.getState();
					
					if (Event.KeeperState.SyncConnected == keeperState) {
						LOG.info( "ZK服务连接还在，消除围栏");
						latch.countDown(); 
					} else if (Event.KeeperState.Disconnected == keeperState) {
						LOG.info("与ZK服务器断开连接");
						// 重连
						connect();
					} else if (Event.KeeperState.Expired == keeperState) {
						LOG.info("会话失效");
						// 重连
						connect();
					}
				}
				
			});
			
			if (stat != null) {
				LOG.info("Thread " + Thread.currentThread().getId() + " ---"+ this.latch +" waiting for " + prevNode);
				
				if(waitTime == 0) { //是否设定等待时间
					this.latch.await();
				} else{
					this.latch.await(waitTime, unit);
				}
				return true;
			}else{
				/**
				 * 此处再次检查的目的：
				 *    条件： 如果我前面有多个节点， 我前面的节点挂了
				 *    情况1： 不执行检查，我将直接获取锁 
				 *    情况2： 执行检查，检查到，我不是最小节点
				 */
				return checkMinPath();
			}
		} catch (KeeperException e) {
			LOG.warn("zk判断节点是否存在 异常：>>>"+e.getMessage());
			throw new NodeNotExistsException("check preNode exsit exception ! ",e);
		} catch (InterruptedException e) {
			LOG.warn("zk判断节点是否存在线程被中断 异常：>>>"+e.getMessage());
			throw new NodeNotExistsException("check preNode has been interruped !",e);
		}
		
	}

	
	
	@Override
	public boolean tryLock() {
		//取出所有子节点
		List<String> lockObjNodes = getSubNodes();
        
        LOG.info(thisNode + "==" + lockObjNodes.get(0));
        
        //判断是否是最小节点
        int index = lockObjNodes.indexOf(thisNode.substring(root.length() + 1));
        
		if (index == 0)
			isLock = true;
		else
			isLock = false;
         
        return isLock;
	}


	/**
	 * 取得所有子节点
	 * @return List<String>
	 */
	private List<String> getSubNodes() {
		//取出所有子节点
        List<String> subNodes = new ArrayList<String>(1);
		try {
			subNodes = zk.getChildren(root, false);
		} catch (KeeperException e) {
			LOG.warn("获取节点异常：>>>"+e.getMessage());
		} catch (InterruptedException e) {
			LOG.warn("该线程被中断，获取节点异常：>>>"+e.getMessage());
		}
        
        //取出所有lockName的锁
        List<String> lockObjNodes = new ArrayList<String>();
        for (String node : subNodes) {
            String _node = node.split(SEPERATE)[0];
            if(_node.equals(lockName)){
                lockObjNodes.add(node);
            }
        }
        //排序
        Collections.sort(lockObjNodes);
		return lockObjNodes;
	}

	@Override
	public boolean isLock() {
		return isLock;
	}

}
