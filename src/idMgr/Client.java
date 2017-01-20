package idMgr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.*;

import org.dom4j.*;
import org.dom4j.io.*;

public class Client {
	/*
	 * start:起始id号
	 * num：每一次本地分配id的数量
	 * edge:边界值  start+num
	 * cur_id:正在分配的id号
	 * */
	private static long num = 0;
	private static long edge = 0;
	private static long cur_id = 0;
	private static boolean flag= true;
	static Document Doc;
	private static String path="config.xml";
	static Lock lock = new ReentrantLock();
	
	//设置id
	public static String setID(ArrayList<?> sqls){
		String sql;
		String data;
		ArrayList<String> loadsql = new ArrayList<String>();
		try {
			File config= new File(path);
			SAXReader reader = new SAXReader();		
			Doc = reader.read(config);
			
			for(int i=0;i<sqls.size();i++){
				
				if(flag){
					//init
					cur_id=readXML();					
					flag=false;
				}else{
					cur_id=getNewId();
				}
				
				if(cur_id == -1){
					System.out.println("没有获取到id块");
					return "error";
				}
				//add id and sql to sql file  
				sql=sqls.get(i).toString();

				data = "insert into idMgr values("+cur_id+",\""+ sql +"\");";
				loadsql.add(data);
			}
			LoadSql.setSql(loadsql);	
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return "ok";
	}
	
	private static long getNewId() {
		// TODO 自动生成的方法存根
		lock.lock();
		long rstl = -1;
		
		//判断是否越界
		if( cur_id >= edge)
		{
			cur_id = getNewBlock(num);
			edge=cur_id + num;
		}
		rstl = ++cur_id;
		//System.out.println("cur_id:"+ cur_id +"   edge:"+ edge);		
		lock.unlock();
		return rstl;
	}
	
	//get id block
	private static long getNewBlock(long size) {		
		// TODO 自动生成的方法存根
		System.out.println("++++正在分配id...+++++");
		long rslt=-1;
		Socket socket = null; 
		try {
			socket = new Socket("localhost",8888);
			OutputStream os=socket.getOutputStream();			
			DataOutputStream write = new DataOutputStream(os);
				
			write.writeLong(size);
			write.flush();
			socket.shutdownOutput();

			DataInputStream reader1 = new DataInputStream(socket.getInputStream());
			rslt=reader1.readLong();
			write.close();
			os.close();
			
            System.out.println("接收服务器端的信息 "+rslt);
			edge=rslt + num;
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			System.out.println("connect failed");
			rslt=-1;
		}finally {  
			if (socket != null) {  
				try {  
					socket.close();  
				} catch (IOException e) {  
					socket = null;   
					System.out.println("客户端 finally 异常:" + e.getMessage());   
				}  
			}  
		} 
 		System.out.println("+++++id分配完毕...+++++");
 		try {
			Element root = Doc.getRootElement();
			
			root.element("start").setText(rslt +"");			
			XMLWriter writer = new XMLWriter(new FileOutputStream(path));			
			writer.write(Doc);
			writer.close();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return rslt;		
	}
	
	//read config.xml file,get the start id
	public static long readXML(){
		long start;

		Element root = Doc.getRootElement();
		
		start = Integer.parseInt(root.elementText("start"));
		num = Integer.parseInt(root.elementText("num"));

		System.out.println("起始id:"+start);
		
		//init, get the id block
		start=getNewBlock(num);
		return start;
	}
}