package lock.curLock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bs.util.Global;
import com.bs.util.PropertiesUtil;

/**
 * 共享值，测试锁
 * @author sere
 *
 */
public class ShareValue {
	private static final Logger LOG = LoggerFactory.getLogger(ShareValue.class);
	//public static int ticket = Integer.valueOf(Global.getConfig("ticket"));
	
	
	public static int getTicket(){
		return Integer.valueOf(PropertiesUtil.getKey("ticket"));
	}
	
	
	public static void write(int ticket) throws IOException{
		File file = new File("D:\\qqd_new_workspace\\un_company_maven\\zklock\\src\\main\\resources\\appconfig.properties");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		LOG.info("ticket="+ticket);
		writer.write("ticket="+ticket);
		writer.flush();
		writer.close();
	}
}
