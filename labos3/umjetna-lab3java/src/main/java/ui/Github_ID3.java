package ui;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;

class Github_ID3 {

	/** Each node of the tree contains either the attribute number (for non-leaf
	 *  nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 *  tree nodes in <b>children</b> containing each of the children of the
	 *  node (for non-leaf nodes).
	 *  The attribute number corresponds to the column number in the training
	 *  and test files. The children are ordered in the same order as the
	 *  Strings in strings[][]. E.g., if value == 3, then the array of
	 *  children correspond to the branches for attribute 3 (named data[0][3]):
	 *      children[0] is the branch for attribute 3 == strings[3][0]
	 *      children[1] is the branch for attribute 3 == strings[3][1]
	 *      children[2] is the branch for attribute 3 == strings[3][2]
	 *      etc.
	 *  The class number (leaf nodes) also corresponds to the order of classes
	 *  in strings[][]. For example, a leaf with value == 3 corresponds
	 *  to the class label strings[attributes-1][3].
	 **/
	class Tree {

		Tree[] children;
		int value;

		public Tree(Tree[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()

		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" +
							strings[value][i] + "\n" +
							children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes-1][value] + "\n";
		} // toString(String)

	} // inner class Tree

	private int attributes; 	// Number of attributes (including the class)
	private int examples;		// Number of training examples
	private Tree decisionTree;	// Tree learnt in training, used for classifying
	private String[][] data;	// Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount;  // Number of unique strings for each attribute

	public Github_ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor

	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);

	static double xlogx(double x) {
		return x == 0? 0: x * Math.log(x) / LOG2;
	} // xlogx()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		// PUT  YOUR CODE HERE FOR CLASSIFICATION
		for(int row=1; row<testData.length; row++){
			int result = classify(testData[row], decisionTree);
			System.out.println(strings[attributes-1][result]);
		}
	} // classify()

	private int classify(String[] testData, Tree tree){
		//only row is passed to this method
		//if reached tree leaf node
		if(tree.children == null){
			return tree.value;
		}
		//get string of selected attribute for this row
		String string = testData[tree.value];
		//find string number, to follow right branch
		for(int branch=0; branch<stringCount[tree.value]; branch++){
			if(string.equals(strings[tree.value][branch])){
				return classify(testData, tree.children[branch]);
			}
		}
		/*
		this would happen if training data didn't have a string that was
		in the test data
		*/
		return 0;
	}

	//___________________________________________________________________________
	//___________________________________________________________________________
	//___________________________________________________________________________
	
	public void train(String[][] trainingData) {
		indexStrings(trainingData);
		decisionTree = train(new ArrayList<Integer>(), new ArrayList<Integer>());
		//printTree();
		// PUT  YOUR CODE HERE FOR TRAINING
	} // train()

	//___________________________________________________________________________
	
	private Tree train(ArrayList<Integer> ignoredCols, ArrayList<Integer> ignoredRows){
		if(ignoredRows.size() == examples-1){
			//no rows left
			return null;
		}
		if(ignoredCols.size() >= attributes-1){
			//if no attributes are left
			return new Tree(null, getMostCommon(ignoredRows));
		}
		int cls = isSameClass(ignoredRows);
		if(cls != -1){
			//if all are same class
			return new Tree(null, cls);
		}
		int bestAttr = getBestAttr(ignoredCols, ignoredRows);
		System.out.println("best attr " + bestAttr);
		//add best attribute to ignored columns, because it cannot be reused
		ArrayList<Integer> bic = new ArrayList<Integer>(ignoredCols);
		bic.add(bestAttr);
		//create a branch for each string of this attribute
		Tree[] subsets = new Tree[stringCount[bestAttr]];
		for(int i=0; i<subsets.length; i++){
			//split data for each string of an attribute
			//method creates a shallow copy of the array, to cause data disintegrity
			ArrayList<Integer> bir = addIgnoredRows(bestAttr, i, ignoredRows);
			//pass new ignored columns and rows to the method
			subsets[i] = train(bic, bir);
			if(subsets[i] == null){
				subsets[i] = new Tree(null, getMostCommon(ignoredRows));
			}
		}
		return new Tree(subsets, bestAttr);
	}
	
	//___________________________________________________________________________
	//___________________________________________________________________________
	//___________________________________________________________________________
	
	
	
	

	private ArrayList<Integer> addIgnoredRows(int attr, int sId, ArrayList<Integer> ignoredRows){
		String string = strings[attr][sId];
		if(string == null) return ignoredRows;
		ArrayList<Integer> newIgnored = new ArrayList<Integer>(ignoredRows);
		for(int row=1; row<examples; row++){
			//if string is not equal cell value it is added to ignored
			//but if it is already ignored, there is no need for it to be there twice
			if(!string.equals(data[row][attr]) && !isIgnored(row, ignoredRows)){
				newIgnored.add(row);
			}
		}
		return newIgnored;
	}

	private int isSameClass(ArrayList<Integer> ignoredRows){
		int cls = -1;
		boolean found = false;
		for(int row=1; row<examples || !found; row++){
			//find first not ignored row
			if(isIgnored(row, ignoredRows)) continue;
			//set class string as class of this row
			String clsString = data[row][attributes-1];
			//find index of this class
			for(int clsId=0; clsId<stringCount[attributes-1]; clsId++){
				if(clsString.equals(strings[attributes-1][clsId])){
					cls = clsId;
					found = true;
					break;
				}
			}
		}
		//save class string to be reused for comparing
		String clsString = strings[attributes-1][cls];
		//check each not ignored row, if all are the same
		for(int row=1; row<examples; row++){
			if(isIgnored(row, ignoredRows)) continue;
			if(!clsString.equals(data[row][attributes-1])){
				//if class string of this row is different
				return -1;
			}
		}
		//return class that is same for all rows;
		return cls;
	}

	private int getMostCommon(ArrayList<Integer> ignoredRows){
		int cCol = attributes-1;
		//count how many of each class in not ignored data
		int[] clsCount = new int[stringCount[cCol]];
		for(int row=1; row<examples; row++){
			if(isIgnored(row, ignoredRows)) continue;
			String cellClass = data[row][cCol];
			for(int cls=0; cls<clsCount.length; cls++){
				//find class index
				if(cellClass.equals(strings[cCol][cls])){
					clsCount[cls]++;
				}
			}
		}
		//find highest number of all array
		int mostCommonId = 0;
		for(int cls=1; cls<clsCount.length; cls++){
			if(clsCount[cls] > clsCount[mostCommonId]){
				mostCommonId = cls;
			}
		}
		return mostCommonId;
	}

	private void printCCount(int[][][] cCount){
		for(int[][] attr : cCount){
			System.out.println("Attribute");
			for(int[] string : attr){
				System.out.println("  String");
				for(int cCnt : string){
					System.out.println("    Class count: " + cCnt);
				}
			}
		}
	}

	private int getBestAttr(ArrayList<Integer> ignoredCols, ArrayList<Integer> ignoredRows){
		//-1 - because 1 is title row
		double s = getS(ignoredRows);
		int[][][] cCount = getClassCount(ignoredCols, ignoredRows);
		int totalRows = examples - 1 - ignoredRows.size();
		//printCCount(cCount);
		double bestGain = -1;
		int bestAttr = -1;
		//
		for(int attr=0; attr<cCount.length; attr++){
			//calculate for every not ignored attribute
			if(isIgnored(attr, ignoredCols)) continue;
			double gain = s;
			for(int[] string : cCount[attr]){
				//for each string in that attribute
				int stringTotal = 0; //total number of those strings in data
				for(int classSum : string){
					stringTotal += classSum;
				}
				//calculate H(s)
				double entropy = 0.0;
				for(int classSum : string){
					//class fraction of a string e.g.:
					//2/3 = 2 rows are some class out of 3 rows of this string
					double cfos = (double)classSum/(double)stringTotal;
					entropy -= xlogx(cfos);
				}
				//gain -= (stringRows / allRows) * entropy
				//ratio of this string to total number of rows in data
				double ratio = ((double)stringTotal/(double)totalRows);
				gain -= ratio * entropy;
			}
			if(gain > bestGain){
				//if gain for this attribute is better than previous - set as new best attribute
				bestGain = gain;
				bestAttr = attr;
			}
		}
		return bestAttr;
	}

	private boolean isIgnored(int number, ArrayList<Integer> ignoredVals){
		for(Integer ignored : ignoredVals){
			if(number == ignored) return true;
		}
		return false;
	}

	private int[][][] getClassCount(ArrayList<Integer> ignoredCols, ArrayList<Integer> ignoredRows){
		int cCol = attributes-1; //class column
		int classes = stringCount[cCol];
		//[attribute][string][class]
		int[][][] cCount = new int[cCol][][];
		for(int attr=0; attr<cCol; attr++){
			/*
			find out if the column is to be ignored
			alternatively I had option to remove column from data, but that
			created multiple copies of smaller and smaller data, plus it required
			twice as many iterations
			*/
			if(isIgnored(attr, ignoredCols)) continue;
			//System.out.println("[ATTR]");
			int attrStrings = stringCount[attr];
			cCount[attr] = new int[attrStrings][classes];
			//check each row, without title row (1st row)
			for(int row=1; row<examples; row++){
				//find out if the row should be ignored
				if(isIgnored(row, ignoredRows)) continue;
				//data[row][attr];
				String cell = data[row][attr];
				String rowClass = data[row][cCol];
				int stringId = 0;
				for(String val : strings[attr]){
					if(val == null) continue;
					/*
					1) Try matching each string for this attribute with cell value
						 to find string index
					*/
					if(cell.equals(val)){
						//matched cell value with one of the attribute string
						int classId = 0;
						for(String cls : strings[cCol]){
							if(cls == null) continue;
							/*
							2) Try matching each string for class with class of this col
								 to find index of this class
							*/
							if(rowClass.equals(cls)){
								cCount[attr][stringId][classId]++;
							}
							classId++;
						}
					}
					stringId++;
				}
			}
		}
		return cCount;
	}

	private double getS(ArrayList<Integer> ignoredRows){
		int cCol = attributes-1;
		int[] cCnt = new int[stringCount[cCol]];
		for(int row=1; row<examples; row++){
			//check if row is ignored
			if(isIgnored(row, ignoredRows)) continue;
			String rowCls = data[row][cCol];
			//find which class matches class of a row
			for(int c=0; c<cCnt.length; c++){
				if(rowCls.equals(strings[cCol][c])){
					cCnt[c]++;
				}
			}
		}
		double s = 0.0;
		int totalRows = examples - 1 - ignoredRows.size();
		for(Integer sum : cCnt){
			double fraction = ((double)sum/totalRows);
			s -= xlogx(fraction);
		}
		return s;
	}

	/** Given a 2-dimensional array containing the training data, numbers each
	 *  unique value that each attribute has, and stores these Strings in
	 *  instance variables; for example, for attribute 2, its first value
	 *  would be stored in strings[2][0], its second value in strings[2][1],
	 *  and so on; and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break;	// we've seen this String before
				if (index == stringCount[attr])		// if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/** For debugging: prints the list of attribute values for each attribute
	 *  and their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index +
									" = " + strings[attr][index]);
	} // printStrings()

	/** Reads a text file containing a fixed number of comma-separated values
	 *  on each line, and returns a two dimensional array of these values,
	 *  indexed by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName)
								throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null)
			lines++;
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
		
		System.out.println("parseCSV");
		for (String[] a : data) {
			for (String b : a) {
				System.out.print(b + " ");
			}
			System.out.println();
		}
		
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException,
												  IOException {
		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		Github_ID3 classifier = new Github_ID3();
		classifier.train(trainingData);
		classifier.printTree();
		classifier.classify(testData);
	} // main()

} // class ID3