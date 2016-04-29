
public class BinClass {
	
	int id;
	BagOfWords bowSingle;
	BagOfWords bowDuples;
	BagOfWords bowTriples;
	
	BinClass(int idClass){
		id = idClass;
		bowSingle = new BagOfWords();
		bowDuples = new BagOfWords();
		bowTriples = new BagOfWords();
	}
	
	
}
