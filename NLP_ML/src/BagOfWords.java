import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class BagOfWords {

	public HashMap<String, Integer> BowDictionary;
	
	BagOfWords(){
		BowDictionary = new HashMap<String, Integer>();
	}
	
	void addWordCount(String BowKey){
		this.BowDictionary.put(BowKey, this.getWordCount(BowKey)+1);
	}
	
	void addWordCount(String BowKey, int count){
		this.BowDictionary.put(BowKey, count);
	}
	
	int getWordCount(String BowKey){
		if(this.BowDictionary.containsKey(BowKey)){
			return this.BowDictionary.get(BowKey);
		}
		return 0;
	}
	
	void storeToFile(String filename) throws FileNotFoundException, UnsupportedEncodingException{
		Set<String> BowKeySet = this.BowDictionary.keySet();
		
		PrintWriter writer = new PrintWriter(filename, "UTF-8");
		
		//System.out.println(filename);
		
	  	for(String BowKey: BowKeySet){
	  		writer.println(BowKey + ":" + this.getWordCount(BowKey));
	  	}	
	  	
	  	writer.close();
	  	
	}
	
	
	void readFromFile(String filename) throws IOException{
		
		FileInputStream fstream = null;
		fstream = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream,"UTF-8"));
		
		String strLine = null; 
		while((strLine = br.readLine()) != null) {
			String[] elem = strLine.split(":");
			int count = 0;
			Pattern p = Pattern.compile("([0-9])");
			Matcher m = p.matcher(elem[1]);
			if(m.find()) count = Integer.parseInt(elem[1]);
			this.addWordCount(elem[0], count);
			//System.out.println(elem[0]+":"+count);
		}

		br.close();		//Close the input stream
		return;
	  	
	}
	
	
	
}
