package idMgr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Server {
	
	public static void main(String[] args)  {  
		Server.doPost();  
   } 
	
	public static void doPost(){
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
}
