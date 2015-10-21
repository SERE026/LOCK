package lock.curLock;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelJob  implements Runnable{
	
	private Logger log = LoggerFactory.getLogger(ParallelJob.class);
	
	private String jobId;
	// 读锁  
    private InterProcessLock readLock;  
    
    private int wait_time = 2000;
    
	public ParallelJob(String jobId, InterProcessLock readLock) {
		super();
		this.jobId = jobId;
		this.readLock = readLock;
	}


	@Override  
    public void run() {  
        try {  
            doWork();  
        } catch (Exception e) {
        	e.printStackTrace();
            // ingore;  
        }  
    }  
  
    public void doWork() throws Exception {  
        try {  
            if (!readLock.acquire(wait_time, TimeUnit.SECONDS)) {  
                System.err.println(jobId + "等待" + wait_time + "秒，仍未能获取到lock,准备放弃。");  
                throw new Exception("didn't get lock!");
            }  
            
            log.info("目前还有  【"+ShareValue.getTicket()+"】 张票！");
            // 模拟job执行时间0-4000毫秒  
            int exeTime = new Random().nextInt(2000);  
            System.out.println(jobId + "开始执行,预计执行时间= " + exeTime + "毫秒----------");  
            Thread.sleep(exeTime);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
        	readLock.release();  
        }  
    }  

}
