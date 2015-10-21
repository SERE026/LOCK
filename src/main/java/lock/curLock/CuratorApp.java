package lock.curLock;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 *  直接使用curator 客户端 不可重入
 * @author sere
 *
 */
public class CuratorApp {
	private static final Logger LOG = LoggerFactory.getLogger(CuratorApp.class);
    
    private static final String CONFIG = "192.168.1.222:2181,192.168.1.222:2182,192.168.1.222:2183";
    
    private static final String PATH = "/locks";  
  
    
    public static void main(String[] args) throws InterruptedException {  
    	CuratorFramework client = CuratorFrameworkFactory.newClient(CONFIG, new ExponentialBackoffRetry(1000, Integer.MAX_VALUE)) ;
    	// 进程内部（可重入）读写锁   (使用synchronized 或者ReentrantLock 可以避免进程内部重复)
        InterProcessReadWriteLock lock ;  
        // 读锁  
        InterProcessLock readLock;  
        // 写锁  
        InterProcessLock writeLock;  
    	
    	client.start();  
    	
        lock = new InterProcessReadWriteLock(client, PATH);  
        readLock = lock.readLock();  
        writeLock = lock.writeLock();
        try {  
            List<Thread> jobs = Lists.newArrayList();  
            for (int i = 0; i < 10; i++) {  
                Thread t = new Thread(new ParallelJob("Parallel任务" + i, readLock));  
                jobs.add(t);  
            }  
  
            for (int i = 0; i < 10; i++) {  
                Thread t = new Thread(new MutexJob("Mutex任务" + i, writeLock));  
                jobs.add(t);  
            }  
  
            for (Thread t : jobs) {  
                t.start();  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {
        	//多线程的原因先不关闭
            //CloseableUtils.closeQuietly(client);  
        }  
    }
}
