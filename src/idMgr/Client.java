package idMgr;



import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	private static boolean flag= false;
	static Document Doc;
	static Element note;
	private static String path="config.xml";
	
	static Lock lock = new ReentrantLock();
	
	//设置id
	public static String setID(ArrayList sqls){
		Element eRecode;
		Element newRecode;
		String sql;

		try {
			File config= new File(path);
			SAXReader reader = new SAXReader();		
			Doc = reader.read(config);
			//获取根节点元素对象
			Element root = Doc.getRootElement();
			Element note = root.element("note");
			
			for(int i=0;i<sqls.size();i++){
				//初始化
				if(!flag){
					System.out.println("=======初始化=======");
					cur_id=readXML();					
					flag=true;
					System.out.println("======初始化完成======");
				}else{
					cur_id=getNewId();
				}
				
				if(cur_id == -1){
					System.out.println("没有获取到id块");
					return "error";
				}
				
				sql=sqls.get(i).toString();
				
				//在sql节点下添加recode节点，并设置id值和sql语句
				eRecode=(Element) root.elements("sql").get(0);
				newRecode=DocumentHelper.createElement("recode");
				newRecode.addAttribute("id",""+ cur_id);
				newRecode.addAttribute("value", sql);
				eRecode.add(newRecode);
			}
			//note.element("start").setText(cur_id+"");			
			//输出到xml文件
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			format.setIndent(true); //设置是否缩进
			format.setIndent("	"); //以TAB方式实现缩进
			format.setNewlines(true); //设置是否换行
			FileOutputStream fos = new FileOutputStream(path);
			XMLWriter writer = new XMLWriter(fos,format);
			writer.write(Doc);
			writer.close();
			
			System.out.println("文件写入完成,写入后的文件");
			Iterator<?> it = root.element("sql").elements("recode").iterator();
			while(it.hasNext()){
				Element sql1=(Element) it.next();
				System.out.println("id:"+sql1.attributeValue("id")+" 对应SQL语句:"+sql1.attributeValue("value"));
			}
			
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
	
	//获取id块
	private static long getNewBlock(long size) {		
		// TODO 自动生成的方法存根
		System.out.println("     正在分配id...     ");
		long rslt=-1;
		Socket socket = null; 
		try {
			socket = new Socket("localhost",8888);
			OutputStream os=socket.getOutputStream();			
			DataOutputStream write = new DataOutputStream(os);
					
			// 把数据写到报文
			write.writeLong(size);
			write.flush();
			socket.shutdownOutput();
			//接收服务器返回数据
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
 		System.out.println("  id分配完毕...     ");
 		try {
			Element root = Doc.getRootElement();
			Element note = root.element("note");
			
			note.element("start").setText(rslt +"");			
			XMLWriter writer = new XMLWriter(new FileOutputStream(path));			
			writer.write(Doc);
			writer.close();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return rslt;		
	}
	
	//读取config文件，初始化时使用 ,返回 需要使用的起始id号
	public static long readXML(){
		long start;

		Element root = Doc.getRootElement();
		Element note = root.element("note");
		
		//获取id:起始、结束、每次分配数量,当前用到哪个id;
		start = Integer.parseInt(note.elementText("start"));
		num = Integer.parseInt(note.elementText("num"));
		
		//获取当前节点的所有子节点
		List recode = root.element("sql").elements("recode");

		System.out.println("起始id:"+start);
		System.out.println("最后一条记录id:"+ getUseNum(root));
		
//		Iterator<?> it = recode.iterator();
//		while(it.hasNext()){
//			Element sql=(Element) it.next();
//			System.out.println("id:"+sql.attributeValue("id")+" 对应SQL语句:"+sql.attributeValue("value"));
//		}

		//初始化重新分配id块
		start=getNewBlock(num);
		return start;
	}
	
	//获取当前使用到的最大id
	private static int getUseNum(Element ele) {
		// TODO 自动生成的方法存根
		List lRecode = ele.element("sql").elements("recode");
		if(lRecode.size()==0){
			return -1;
		}
		Element lastRecodeId =  (Element) lRecode.get(lRecode.size()-1);
		int use_num = Integer.parseInt(lastRecodeId.attributeValue("id"));
		return use_num;
	}
	
}
