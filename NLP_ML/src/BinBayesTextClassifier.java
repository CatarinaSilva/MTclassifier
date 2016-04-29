import java.io.*;
import java.util.*;
import java.util.regex.*;

public class BinBayesTextClassifier {

	int lineCnt;
	BinClass bin0;
	BinClass bin1;
	String usage = " USAGE "; //TODO
	
	String testSent;
	
	
	BinBayesTextClassifier()
	{
		lineCnt = 0;
		bin0 = new BinClass(0);
		bin1 = new BinClass(1);
	}
	
	
	
	int getClass(String line)
	{
		
		String [] numberSplit = line.split("\\t");
		if(numberSplit.length > 1){
			String number = numberSplit[0].replaceAll("[^\\d]","");
			Pattern p = Pattern.compile("([0-9])");
			Matcher m = p.matcher(number);
			if(m.find()) return Integer.parseInt(number);
		}
		return -1;
	}
	
	
	
	String[] processLine(String line)
	{
		String[] firstSplit = line.split("\\t");
		String sentenceStr = firstSplit[1].replaceAll("[^-\\p{L}\\d]", " ").toLowerCase();
		String wordsStr = sentenceStr.replaceAll("[\\d]+", "#NUM#");
		return wordsStr.split("[ ]+");
		
	}
	
	
	String[] sanitizeWords(String[] words)
	{
		for(int idx = 0; idx < words.length; idx++){
			words[idx]=words[idx].replaceAll("^-|-$","").replaceAll("[ ]+", "");
		}
		return words;
	}
	
	
	void addWords(String[] words, int idClass)
	{
	
		BinClass bow = null;
		if(idClass == bin0.id) bow = this.bin0;
		else if(idClass == bin1.id) bow = this.bin1;
		else return;

		for(int idx = 0; idx < words.length; idx++){
			words[idx]=words[idx].replaceAll("^-|-$","").replaceAll("[ ]+", "");
			if(!words[idx].isEmpty()){
				if(words[idx]!="#NUM#") bow.bowSingle.addWordCount(words[idx]);
				if(idx!=0){
					bow.bowDuples.addWordCount(words[idx-1]+" "+words[idx]);
					if(idx!=1) bow.bowTriples.addWordCount(words[idx-2]+" "+words[idx-1]+" "+words[idx]);
				}
			}	
		}
		
		return;
	}
	
	
	
	void train(String trainingFile, int dontUse) throws IOException{
		
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(trainingFile);
		} catch (FileNotFoundException e) {
			System.out.println("Wrong file specified: " + trainingFile + " not found. ");
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream,"UTF8"));
		
		String strLine = null; 
		int idLine = 0;
		while((strLine = br.readLine()) != null) {
			if(this.lineCnt+1 == dontUse){
				this.testSent = strLine.trim();
				this.lineCnt++;
			}else{
				if((idLine = getClass(strLine))!=-1){
					this.addWords(this.processLine(strLine), idLine);
					this.lineCnt++;
				}
			}
		}

		br.close();		//Close the input stream
		return;
	}
	
	double computeProbability(String [] words, int binID)
	{
		BinClass target = null;
		BinClass other = null;
		if(binID == 0){
			target = this.bin0;
			other  = this.bin1;
		}else{
			target = this.bin1;
			other  = this.bin0;
		}
		
		double prob = 0;
		
		for(int idx = 0; idx < words.length; idx++){
			int cntTargetSingle=0;
			int cntOtherSingle = 0;
			int cntTargetDuples=0;
			int cntOtherDuples = 0;
			int cntTargetTriples=0;
			int cntOtherTriples = 0;
			
			
			if(!words[idx].isEmpty()){
				if(words[idx]!="#NUM#"){
					cntTargetSingle = target.bowSingle.getWordCount(words[idx]);
					cntOtherSingle = other.bowSingle.getWordCount(words[idx]);
				}
				if(idx!=0){
					cntTargetDuples = target.bowDuples.getWordCount(words[idx-1]+" "+words[idx]);
					cntOtherDuples = other.bowDuples.getWordCount(words[idx-1]+" "+words[idx]);
					if(idx!=1){
					    cntTargetTriples = target.bowTriples.getWordCount(words[idx-2]+" "+words[idx-1]+" "+words[idx]);
						cntOtherTriples = other.bowTriples.getWordCount(words[idx-2]+" "+words[idx-1]+" "+words[idx]);
					}
				}
			}

			double singleProb = ((1.0*cntTargetSingle+1)/(cntTargetSingle + cntOtherSingle + 1));
			double doubleProb = ((1.0*cntTargetDuples+1)/(cntTargetDuples + cntOtherDuples + 1));
			double tripleProb = ((1.0*cntTargetTriples+1)/(cntTargetTriples + cntOtherTriples + 1));
			prob = prob + 1.0*Math.log(singleProb) + 3.0*Math.log(doubleProb) + 3.0*Math.log(tripleProb);
		}
		
		
		return prob;
	}
	
	void test(String testFile, String outFile) throws IOException{
		
		PrintWriter writer = new PrintWriter(outFile, "UTF-8");
		
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(testFile);
		} catch (FileNotFoundException e) {
			System.out.println("Wrong file specified: " + testFile + " not found. ");
			writer.close();
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream,"UTF8"));
		
		String strLine = null; 
		while((strLine = br.readLine()) != null) {
			int probClass = testStr(strLine);
 			writer.println(probClass + "\t" + strLine.split("\\t")[1]);
		}

		writer.close();
		br.close();		//Close the input stream
		return;
	}
	
	int testStr(String line){
			String [] words = this.processLine(line);
			words = this.sanitizeWords(words);
			double prob0 = this.computeProbability(words, 0);
			double prob1 = this.computeProbability(words, 1);
			int probClass;
			Random rand2 = new Random();
			if(prob0 == prob1) probClass = rand2.nextInt(2);
			else probClass = (prob0 > prob1) ? 0 : 1;
			//System.out.println(probClass + "\t" + prob0 + "\t" + prob1);
			return probClass;
	}
	
	double percentageRight(String testFileSol, String outFile) throws IOException{
		
		List<Integer> sol = new ArrayList<Integer>();
		List<Integer> out = new ArrayList<Integer>();
		
		FileInputStream fstream = null;
		fstream = new FileInputStream(testFileSol);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream,"UTF8"));
		String strLine = null; 
		
		while((strLine = br.readLine()) != null) {
			int id = getClass(strLine);
			sol.add(id);
		}
		br.close();
		
		FileInputStream gstream = null;
		gstream = new FileInputStream(outFile);
		BufferedReader bre = new BufferedReader(new InputStreamReader(gstream,"UTF8"));
		String strLineO = null; 
		
		while((strLineO = bre.readLine()) != null) {
			int id = getClass(strLineO);
			out.add(id);
		}
		bre.close();
		
		Integer[] outArr = new Integer[out.size()];
		outArr = out.toArray(outArr);
		
		Integer[] solArr = new Integer[sol.size()];
		solArr = sol.toArray(solArr);
		
		if(solArr.length != outArr.length) System.out.println("ERROR");
		else{
			int c = 0;
			int e = 0;
			for(int j=0; j<outArr.length; j++){
				if(outArr[j].intValue()==solArr[j].intValue()) c++;
				else e++;
			}
			return (1.0*e/(e+c));
		}
		
		return 0;
	}
	
	public static void main(String[] args) throws IOException {
		
		BinBayesTextClassifier example = new BinBayesTextClassifier();
		
		if(args.length >= 2){
			switch(args[0]){
			case("random"):
				int count_e = 0;
				int num_iter = 1000;
				for(int a=0; a<num_iter ; a++){
					example.lineCnt = 0;
					Random rand = new Random();
					int randInt = rand.nextInt(20000)+1;
					String trainingSet = args[1];
					example.train(trainingSet, randInt);
					//example.bin1.bowSingle.storeToFile(args[2]+"1single.txt");
					//example.bin1.bowDuples.storeToFile(args[2]+"1duple.txt");
					//example.bin1.bowTriples.storeToFile(args[2]+"1triple.txt");
					//example.bin0.bowSingle.storeToFile(args[2]+"0single.txt");
					//example.bin0.bowDuples.storeToFile(args[2]+"0duple.txt");
					//example.bin0.bowTriples.storeToFile(args[2]+"0triple.txt");
					
					///System.out.println(example.testSent.split("\\t")[1]);
					int resultClass = example.testStr(example.testSent);
					int solClass = example.getClass(example.testSent);
					int e = (resultClass != solClass) ? 1 : 0;
					count_e = (int) (count_e + e);
					System.out.println("iter: " + a + "    sol:" + solClass + "     result:" + resultClass);
					//System.out.println("Training lines: " + example.lineCnt);
				}				
				System.out.println("Wrong percentage: " + 100.0*(1.0*count_e/num_iter) + "%" );
				break;
			case("all"):
				String trainingSet1 = args[1];
				example.train(trainingSet1, -1);
				example.bin1.bowSingle.storeToFile(args[2]+"1single.txt");
				example.bin1.bowDuples.storeToFile(args[2]+"1duple.txt");
				example.bin1.bowTriples.storeToFile(args[2]+"1triple.txt");
				example.bin0.bowSingle.storeToFile(args[2]+"0single.txt");
				example.bin0.bowDuples.storeToFile(args[2]+"0duple.txt");
				example.bin0.bowTriples.storeToFile(args[2]+"0triple.txt");
				String testFile1 = args[3];
				String outFile1 = args[4];
				example.test(testFile1, outFile1);
				String sol1 = args[5];
				double perc1 = example.percentageRight(sol1, outFile1);
				System.out.println("Training lines: " + example.lineCnt);
				System.out.println("Wrong percentage: " + perc1);
				break;
			case("train"):
				String trainingSet11 = args[1];
				example.train(trainingSet11, -1);
				example.bin1.bowSingle.storeToFile(args[2]+"1single.txt");
				example.bin1.bowDuples.storeToFile(args[2]+"1duple.txt");
				example.bin1.bowTriples.storeToFile(args[2]+"1triple.txt");
				example.bin0.bowSingle.storeToFile(args[2]+"0single.txt");
				example.bin0.bowDuples.storeToFile(args[2]+"0duple.txt");
				example.bin0.bowTriples.storeToFile(args[2]+"0triple.txt");
				System.out.println(example.lineCnt);
				break;
			case("test"):
				String trainingFilePrefix = args[1];
				example.bin1.bowSingle.readFromFile(trainingFilePrefix+"1single.txt");
				example.bin1.bowDuples.readFromFile(trainingFilePrefix+"1duple.txt");
				example.bin1.bowTriples.readFromFile(trainingFilePrefix+"1triple.txt");
				example.bin0.bowSingle.readFromFile(trainingFilePrefix+"0single.txt");
				example.bin0.bowDuples.readFromFile(trainingFilePrefix+"0duple.txt");
				example.bin0.bowTriples.readFromFile(trainingFilePrefix+"0triple.txt");
				String testFile11 = args[2];
				String outFile11 = args[3];
				example.test(testFile11, outFile11);
				break;
			default:
				String sol11 = args[0];
				String out = args[1];
				double perc11 = example.percentageRight(sol11, out);
				System.out.println(perc11);
				
			}
		}else{
			System.out.println(example.usage);
		}
		
	}

}
