package File;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class writefile {

	public static void writeStrToFile(String filepath,String str)throws IOException{
	 
	        /* File f=new File(filepath);
	          if(!f.exists())
	          {
	              f.createNewFile();
	              System.out.println("文件"+f.getPath()+"已创建");
	          }
	          FileOutputStream fos=new FileOutputStream(f);
	         DataOutputStream dos=new DataOutputStream(fos);
	         dos.writeUTF(str);
	         System.out.println("文件内容写入完毕");
	         dos.close();
	         fos.close();
	     }*/
		FileWriter writer;
        try {
            writer = new FileWriter(filepath,true);
            writer.write(str);
            writer.write("\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
	
	
}
