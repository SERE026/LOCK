package lock.curLock;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutexJob implements Runnable {

	private Logger log = LoggerFactory.getLogger(MutexJob.class);
	
	private String jobId;
	// 互斥锁
	private InterProcessLock writeLock;

	private int wait_time = 2000;

	public MutexJob(String jobId, InterProcessLock writeLock) {
		super();
		this.jobId = jobId;
		this.writeLock = writeLock;
	}

	@Override
	public void run() {
		try {
			doWork();
		} catch (Exception e) {
			// ingore;
		}
	}

	public void doWork() throws Exception {
		try {
			if (!writeLock.acquire(wait_time, TimeUnit.SECONDS)) {
				System.err.println(jobId + "等待" + wait_time
						+ "秒，仍未能获取到lock,准备放弃。");
				throw new Exception("didn't get lock!");
			}
			
			int ticket = ShareValue.getTicket();
			
			log.info("正在出售第  【"+ticket+"】 张票！");
			ticket--;
			ShareValue.write(ticket);
			//ShareValue.map.put("key", ShareValue.ticket--);
			// 模拟job执行时间0-2000毫秒
			int exeTime = new Random().nextInt(2000);
			System.out.println(jobId + "开始执行,预计执行时间= " + exeTime + "毫秒----------");
			Thread.sleep(exeTime);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writeLock.release();
		}
	}

}
