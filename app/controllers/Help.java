package controllers;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.libs.Json;

public class Help {
	

	public static Map<String, String> readFasta(String fasta){
		String[] lines = fasta.split("\n");
		String name = "";
		
		Map<String, String> ret = new HashMap<String, String>();
		for(String line : lines){
			
			line = line.trim();
			if(line.startsWith(">")){
				name = line.split("\\s|\\|")[0].trim();
				name = name.replaceAll("^>", "");
			}else{
				if(ret.containsKey(name)) 
		    		ret.put(name, ret.get(name) + line);
		    	else
		    		ret.put(name, line);
			}
		    
		}
		return(ret);
	}
	
	
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
        public static boolean isDouble(String str){
                try{
                        Double.parseDouble(str);
                        return true;
                }catch(Exception e){
                        return false;
                }
        }
        
        public static boolean isInteger(String str){
                try{
                        Integer.parseInt(str);
                        return true;
                }catch(Exception e){
                        return false;
                }
        }
        
        public static void writeFile(String toWrite, String path) throws IOException{
                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                out.write(toWrite);
                out.close();
        }
        
        public static String readFile(String path) throws IOException {
                  FileInputStream stream = new FileInputStream(new File(path));
                  try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                    /* Instead of using default, pass in a decoder. */
                    return Charset.defaultCharset().decode(bb).toString();
                  }
                  finally {
                    stream.close();
                  }
        }
        
        public static List<String> getPlateContents(String filename){
                List<String> toReturn = new ArrayList();
                
                String[] sp = filename.split("_");
                toReturn.add(sp[0]);
                
                if(sp.length > 1){
                        if(sp[1].toLowerCase().startsWith("wt")){
                                toReturn.add("Control");
                        }else{
                                toReturn.add("Double mutant");
                        }
                        toReturn.add(sp[1]);
                }else{
                        toReturn.add("-");
                        toReturn.add("-");
                }
                
                if(sp.length > 2){
                        if(sp[2].matches("\\d+")){
                                toReturn.add(sp[2]);
                        }else{
                                toReturn.add("-");
                        }
                }else{
                        toReturn.add("-");
                }
                
                return toReturn;
        }
        
        public static HashMap jsonToMap(String json){
                HashMap map = Json.fromJson(Json.toJson(json), HashMap.class);
                return map;
        }
        

}