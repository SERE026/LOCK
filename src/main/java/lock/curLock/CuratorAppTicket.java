package lock.curLock;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bs.lock.ILock;
import com.bs.lock.curator.CuratorDistributedLock;
import com.bs.lock.zk.ZKDistributedLock;

public class CuratorAppTicket {
	
	ApplicationContext context = new ClassPathXmlApplicationContext("*Context.xml");
	
	public static void main(String[] args) {
		Ticket at = new Ticket();
		Thread t1 = new Thread(at, "一号窗口");  
		Thread t2 = new Thread(at, "二号窗口");  
		Thread t3 = new Thread(at, "三号窗口");  
		Thread t4 = new Thread(at, "四号窗口 "); 
		Thread t5 = new Thread(at, "五号窗口");  
		Thread t6 = new Thread(at, "六号窗口");  
		Thread t7 = new Thread(at, "七号窗口");  
		Thread t8 = new Thread(at, "八号窗口 "); 
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		t6.start();
		t7.start();
		t8.start();
	}
	
}

class Ticket implements Runnable {
	// private Logger log = LoggerFactory.getLogger(ATask.class);
	private static int ticket = ShareValue.getTicket();
	//两种不同客户端的实现方式
	//ILock lock = new ZKDistributedLock("ticket");  
	ILock lock = new CuratorDistributedLock();
	
	public Ticket() {
		super();
	}

	@Override
	public void run() {
		while (true) {
			sale();
			if(ticket <=0) break;
		}
	}

	private void sale() {
		lock.lock();
		ticket = ShareValue.getTicket();
		if (ticket > 0){
			// log.info("任务---"+Thread.currentThread().getId()+"---正在执行，票还剩余：>>["+ticket+"]");
			ticket--;
			try {
				ShareValue.write(ticket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("任务---" + Thread.currentThread().getId() + "---正在执行，票还剩余：>>[" + ticket + "]");
		}
		
		//抢占锁资源比较明显
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lock.unlock();
	}

}
