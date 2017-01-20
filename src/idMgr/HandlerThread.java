package idMgr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public  class HandlerThread implements Runnable {
	private Socket socket;
	private Document Doc;

	private long pos=-1;
	private long block_size=-1;
	private String app_id="app_id";
	
	public HandlerThread(Socket socket) {
		this.socket=socket;
		new Thread(this).start();    
	}

	@Override
	public void run() {
		// TODO 自动生成的方法存根
		// TODO 自动生成的构造函数存根
		long rslt=-1;
		long end;
		Date date=new Date();
		DateFormat tformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time=tformat.format(date);
		
		try {
			String path="serverconfig.xml";
			File config= new File(path);
			SAXReader reader = new SAXReader();		
			Doc = reader.read(config);
			Element root = Doc.getRootElement();
			
			// 读取客户端传过来信息的DataInputStream
			DataInputStream in = new DataInputStream(socket.getInputStream());
			// 向客户端发送信息的DataOutputStream  
	        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			block_size = in.readLong();
			System.out.println("接收客户端的信息"+block_size);
			
	        pos = Integer.parseInt(root.elementText("start"));
			end = Integer.parseInt(root.elementText("end"));
			
			long temp = pos + block_size;
			
			if(temp >= end){
				System.out.println("warring:服务器端id用完，重新分配id.");
				end += end ;
				root.element("end").setText(end+"");

				OutputFormat format1 = OutputFormat.createPrettyPrint();
				FileOutputStream dEnd = new FileOutputStream(path);
				XMLWriter wr = new XMLWriter(dEnd,format1);
				wr.write(Doc);
				wr.flush();
				wr.close();
			}
			
			rslt = pos;
			pos = temp;
					
			out.writeLong(rslt);
			out.flush();

			in.close();
			out.close();
			
			getIDSegmentInfo(block_size,time);
			
			root.element("start").setText(pos+"");

			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			format.setIndent(true); //设置是否缩进
			format.setIndent("	"); //以TAB方式实现缩进
			format.setNewlines(true); //设置是否换行
			FileOutputStream fos = new FileOutputStream(path);
			XMLWriter writer = new XMLWriter(fos,format);
			writer.write(Doc);
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}	
	}
	private void getIDSegmentInfo(long block_size, String time){
		Node node;
		Element lastRecord;
		int i = 0;

		Element root = Doc.getRootElement();
		List records = root.elements("record");
		System.out.println(records.size());
		
		if(records.size() == 0){
			setRecord(0, time);			
		}else{		
			lastRecord = (Element) records.get(records.size()-1);
			i = Integer.parseInt(lastRecord.attributeValue("id")) + 1;
			setRecord(i, time);
		}
	}

	private void setRecord(int i,String date) {
		// TODO 自动生成的方法存根
		Element root;
		Element record;
		Element time;
		Element app;
		Element startId;
		Element getIdBlock;

		root = Doc.getRootElement();
		record = DocumentHelper.createElement("record");
		record.addAttribute("id", i+"");
		
		time = DocumentHelper.createElement("time");
		app = DocumentHelper.createElement("app");
		startId = DocumentHelper.createElement("startId");
		getIdBlock = DocumentHelper.createElement("getIdBlock");
		
		time.setText(date);
		app.setText(app_id);
		startId.setText(pos+"");
		getIdBlock.setText(block_size+"");			
		
		record.add(time);
		record.add(app);
		record.add(startId);
		record.add(getIdBlock);			
		
		root.add(record);
	}

}


