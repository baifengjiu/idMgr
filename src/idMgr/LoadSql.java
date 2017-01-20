package idMgr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class LoadSql {

	private static String path="testSql.sql";	
	
	public static void setSql(ArrayList<String> load){
		String data;
		String head = "DROP TABLE IF EXISTS idMgr;"
					+"CREATE TABLE idMgr("
					+"cur_id			INT(225),"
					+"content			TEXT);";
		
		try {
			File file=new File(path);
		
		    if(!file.exists()){
				file.createNewFile();
				
				FileWriter fw=new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(head);
				bw.flush();
				bw.close();
		    }


			FileWriter fileWritter=new FileWriter(file,true);
			BufferedWriter out = new BufferedWriter(fileWritter);
			
			for(int i=0;i<load.size();i++){
				data=load.get(i).trim();
				out.write("\r\n");
				out.write(data);
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}

}
