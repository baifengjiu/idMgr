package idMgr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


public class Server {
	
	public void main(String[] args)  { 
		Server server=new Server();
		server.doPost();  
   } 
	
	public void doPost(){
		Lock lock=new ReentrantLock();
		lock.lock();

		try {
			ServerSocket server = new ServerSocket(8888);
			while (true) {    
				// 一旦有堵塞, 则表示服务器与客户端获得了连接 
				Socket socket = server.accept();
				new HandlerThread(socket);
			}
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}		
		
		lock.unlock();	
	}
	
	public  class HandlerThread implements Runnable {
		private Socket socket;
		public HandlerThread(Socket socket) {
			this.socket=socket;
			new Thread(this).start();    
		}

		@Override
		public void run() {
			// TODO 自动生成的方法存根
			// TODO 自动生成的构造函数存根
			long block_size=-1;
			long rslt=-1;
			long pos=-1;
			try {
				String path="serverconfig.xml";
				File config= new File(path);
				SAXReader reader = new SAXReader();		
				Document Doc = reader.read(config);
				Element root = Doc.getRootElement();
				
				//获取id:起始、结束、每次分配数量,当前用到哪个id;
				pos = Integer.parseInt(root.elementText("start"));
				System.out.println("起始id:"+pos);
			
			
				// 读取客户端传过来信息的DataInputStream
				DataInputStream in = new DataInputStream(socket.getInputStream());
				// 向客户端发送信息的DataOutputStream  
		        DataOutputStream out = new DataOutputStream(socket.getOutputStream());      
				
				block_size = in.readLong();
					
				System.out.println("接收客户端的信息"+block_size);
				rslt = pos;
				pos = pos + block_size;
						
				out.writeLong(rslt);
				out.flush();
				
				//关闭资源
				in.close();
				out.close();
				
				root.element("start").setText(pos+"");			
				XMLWriter writer = new XMLWriter(new FileOutputStream(path));
				writer.write(Doc);
				writer.close();
				
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}	
		}
		
	}
}
