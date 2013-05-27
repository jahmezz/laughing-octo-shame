package kahng.james.FSA;

//Imported for file reading
import java.io.BufferedReader;
import java.io.FileReader;
//Required exceptions for readers
import java.io.FileNotFoundException;
import java.io.IOException;
//Stored alphabet to Map for simpler indexing
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Implementation of a dynamic FSA that can recognize java reserved words, new identifiers and identifiers
 * that appear again.
 * How to run code: 1) Extract zip file to any folder 2) In Eclipse, go to File > Import > General 
 * (existing projects into Workspace). Select Next and set the root directory as the folder that was just unzipped.
 * Click finish. 3) Import the project and run kahng.james.fsa.FSAImplementation.java and it should output 
 * just like the provided output file.
 * @author James Kahng 05/23/2013
 */
public class DynamicFSAImplementation {
	
	/** Contains alphabet of language. */
	private final char[] ALPHABET = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
			'v', 'w', 'x', 'y', 'z', '_', '$' };
	
	/** Contains the switch array corresponding to the first letter of each word. */
	private int[] Switch = new int[ALPHABET.length];
	
	/** Contains the symbol array corresponding to the rest of the letters in the word. */
	private char[] Symbol = new char[300];
	
	/** Keeps track of where the next new identifier will be stored in the symbol array. */
	private int symbolIndex = 0;
	
	/** Contains the next array which holds the next transition available when a difference between
	 * stored word and read word is detected. */
	private int[] Next = new int[300];
	
	/** The input from the text file. */
	private String input  = "";
	
	/** The output resulting from executing the machine. */
	private String output = "";
	
	/** The map mapping each character in the alphabet to an integer for easier navigation. */
	private Map<Character, Integer> mappedAlpha;
	
	/** Indicates program is reading code, so it begins storing the output string. */
	private boolean processing = false;

	/**
	 * Executes machine.
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException,IOException {
		DynamicFSAImplementation machine = new DynamicFSAImplementation();
		
		//setup machine alphabet and storage
		machine.setupAlphabet();
		machine.setupArrays();
		//read first input file (java reserved words)
		machine.setupReserved("inputfile1.txt");
		//read second input file (java code)
		machine.readInput("inputfile2.txt");
		//print the results
		machine.printResult();
	}

	/**
	 * Sets up the one-to-one mapping of alphabet characters (A-$ mapped to 0-53).
	 */
	public void setupAlphabet() {
		int index = 0;
		mappedAlpha = new LinkedHashMap<Character, Integer>();
		for (char letter : ALPHABET) {
			mappedAlpha.put(letter, index);
			index++;
		}
	}

	/**
	 * Initializes all values of the switch and next arrays to -1 so unchanged values can be detected.
	 */
	public void setupArrays() {
		for (int i = 0; i < Switch.length; i++) {
			Switch[i] = -1;
		}
		for (int i = 0; i < Next.length; i++)	{
			Next[i] = -1;
		}
	}

	/**
	 * Reads in and stores transitions for reserved Java words.
	 * 
	 * @throws IOException
	 */
	public void setupReserved(String textFile) throws IOException,FileNotFoundException {
		String thisLine;
		String[] words;
		char[] letters;
		char symbol;
		int pointer = 0;
		BufferedReader fileInput = new BufferedReader(new FileReader(textFile));
		// read every line
		while ((thisLine = fileInput.readLine()) != null) {
			// split by spaces
			words = thisLine.split(" +");
			//iterate through the words in the line
			for (String word : words) {
				int index = 0;
				//convert to char array
				letters = word.toCharArray();
				// scan first letter and check if it is in switch
				symbol = letters[index];
				index++;
				// skip words that are not valid identifiers (begin with
				// something other than letters, _, $)
				if (mappedAlpha.get(symbol) == null) {
					continue;
				}
				// pointer now equals the index value of the first letter
				pointer = Switch[mappedAlpha.get(symbol)];
				// if pointer doesn't exist, create the new saved word
				if (pointer == -1) {
					createWord(word, '*');
				// logic only works if identifiers are longer than one letter
				// (letters must be added to Symbol)
				}
				else if (letters.length > 1) {
					symbol = letters[index];
					index++;
					while (true) {
						// check every letter in the word
						if (index != letters.length && Symbol[pointer] == symbol) {
							// check to see if the end marker is stored in symbol
							if (Symbol[pointer] != '*') {
								pointer = pointer + 1;
								symbol = letters[index];
								index++;
							}
							// end marker reached and word is valid (word
							// already in dictionary)
							else {
								break;
							}
						}
						// if the symbol doesn't match the symbol table, check if
						// there is a pointer to another letter in next
						else if (Next[pointer] != -1) {
							pointer = Next[pointer];
						}
						// if neither of these exist, a new word has been found
						else {
							createWord(word, '*');
							break;
						}

					}

				}
			}
		}
		fileInput.close();
	}

	/**
	 * Reads in and translates an input to an output that marks java reserved
	 * identifiers while storing new identifiers and identifying them if they come again.
	 * 
	 * @throws IOException
	 */
	public void readInput(String textFile) throws IOException,FileNotFoundException {
		String thisLine;
		String[] words;
		char[] letters;
		char symbol;
		int pointer = 0;
		//store output
		processing = true;
		BufferedReader fileInput = new BufferedReader(new FileReader(textFile));
		// read every line
		while ((thisLine = fileInput.readLine()) != null) {
			input = input + thisLine + "\n";
			// remove all symbols that do not pertain to identifiers
			thisLine = thisLine.replaceAll("[^A-Za-z0-9_$]+", " ");
			// words split by spaces
			words = thisLine.split(" +");
			// iterate through words
			for (String word : words) {
				int index = 0;
				letters = word.toCharArray();
				// empty line case
				if (letters.length < 1) {
					continue;
				}
				// scan first letter and check if it is in switch
				symbol = letters[index];
				index++;
				// for words that are not valid identifiers (begin with
				// something other than letters, _, $)
				if (mappedAlpha.get(symbol) == null) {
					continue;
				}
				// pointer now equals the index value of the first letter
				pointer = Switch[mappedAlpha.get(symbol)];
				// if pointer is not set, create the new saved word
				if (pointer == -1) {
					createWord(word, '?');
				}
				// logic only works if identifiers are longer than one letter
				// (letters must be added to Symbol)
				else if (letters.length > 1) {
					// check the rest of the word
					symbol = letters[index];
					index++;
					while (true) {
						// check every letter in the word
						// if letter is equal to symbol table
						if (Symbol[pointer] == symbol) {
							// if end marker is a ?, this identifier has been detected again, so end with an @
							if (Symbol[pointer + 1] == '?') {
								word = word + "@";
								stringAccepted(word);
								break;
							}
							// if end marker is an *, the word is a java reserved word, so indicate it as one
							else if (Symbol[pointer + 1] == '*') {
								word = word + '*';
								stringAccepted(word);
								break;
							}
							//end marker not found, so move to next symbol
							else {
								pointer = pointer + 1;
								symbol = letters[index];
								index++;
							}
						}
						// if the symbol doesn't match the symbol table, check if
						// there is a pointer to another letter in next
						else if (Next[pointer] != -1) {
							pointer = Next[pointer];
						}
						//if neither of these exist, a new word has been found
						else {
							createWord(word, '?');
							break;
						}

					}

				}
			}
			// add new line for every line read
			output = output + "\n";
		}
		fileInput.close();
	}

	/**
	 * Updates the switch, symbol and next tables to allow the machine to detect the word passed. 
	 * @param word new identifier
	 * @param end end marker for identifier (?,*,@)
	 */
	public void createWord(String word, char end) {
		// split word into letters
		char[] wordArray = word.toCharArray();
		int letterOfWord = 0;
		// check if first letter is already in storage
		int switchIndex = mappedAlpha.get(wordArray[letterOfWord]);
		letterOfWord++;
		int pointer = Switch[switchIndex];
		// if the pointer is empty, set this word to Switch
		if (pointer == -1) Switch[switchIndex] = symbolIndex;
		// if there is a pointer, check for where we should index this word in Next
		else {
			// iterate through the letters in the new word
			while(letterOfWord < wordArray.length)	{
				// if the letter is found in symbol, move to the next letter
				if(Symbol[pointer] == wordArray[letterOfWord])	{
					pointer++;
					letterOfWord++;
				}
				// if the letter is not found in symbol, jump using the value in next
				else if	(Next[pointer] != -1)	{
					pointer = Next[pointer];
				}
				// if the letter is not found in symbol and there is no next, we have reached the junction
				// for the new word
				else	{
					break;
				}
			}
			// the pointer is now at the first difference between storage and new identifier,
			// we point this next to the beginning of the new word at symbolIndex
			Next[pointer] = symbolIndex;
		}
		
		// add rest of characters to the symbol list
		for (int i = letterOfWord; i < wordArray.length; i++) {
			Symbol[symbolIndex] = wordArray[i];
			symbolIndex++;
		}
		// complete word with a closing character (*, ?, @)
		Symbol[symbolIndex] = end;
		symbolIndex++;
		
		// if we are reading input code, we record the output
		if(processing)	{
			output = output + word + end + " ";
		}
	}

	/**
	 * Adds the identified string to the output string.
	 * @param word The accepted word
	 */
	public void stringAccepted(String word) {
		output = output + word + " ";
	}

	/**
	 * Prints the results of running the dynamic FSA.
	 */
	public void printResult() {
		int alphaCount = 0;
		int switchCount = 0;
		//print switch
		System.out.println("SWITCH:\n");
		while(alphaCount < ALPHABET.length)	{
			//first entry
			if(alphaCount == 0)	{
				System.out.printf("%12s", ALPHABET[alphaCount]);
				alphaCount++;
			}
			// make new line when we hit 20 entries in a row
			else if(alphaCount % 20 == 0)	{
				System.out.print("\nswitch: ");
				for(int i = 0; i < 20; i++)	{
					System.out.printf("%4d", Switch[switchCount]);
					switchCount++;
				}
				System.out.printf("\n\n%12s",ALPHABET[alphaCount]);
				alphaCount++;
			}
			// else, we continue the row
			else 	{
				System.out.printf("%4s", ALPHABET[alphaCount]);
				alphaCount++;
			}
		}
		// last set of switch values
		System.out.print("\nswitch: ");
		for(int i = 0; i < 14; i++)	{
			System.out.printf("%4d", Switch[switchCount]);
			switchCount++;
		}
		//print symbol
		System.out.println("\n\nSYMBOL & NEXT:");
		int symbolCount = 0;
		int symbolIndex = 0;
		int nextIndex   = 0;
		while(symbolCount < Symbol.length)	{
			// first entry
			if(symbolCount == 0)	{
				System.out.printf("\n%12s", symbolCount);
				symbolCount++;
			}
			// divide at every 20 values
			else if(symbolCount % 20 == 0)	{
				System.out.print("\nsymbol: ");
				for(int i = 0; i < 20; i++)	{
					System.out.printf("%4s", Symbol[symbolIndex]);
					symbolIndex++;
				}
				System.out.print("\nnext:   ");
				for(int i = 0; i < 20; i++)	{
					if(Next[nextIndex] == -1)	{
						System.out.printf("%4s", "");
						nextIndex++;
					}
					else	{
						System.out.printf("%4s", Next[nextIndex]);
						nextIndex++;
					}
				}
				System.out.printf("\n\n%12s",symbolCount);
				symbolCount++;
			}
			// else, continue row
			else 	{
				System.out.printf("%4s", symbolCount);
				symbolCount++;
			}
		}
		// final entry for symbol table
		System.out.print("\nsymbol: ");
		for(int i = 0; i < 20; i++)	{
			System.out.printf("%4s", Symbol[symbolIndex]);
			symbolIndex++;
		}
		System.out.print("\nnext:   ");
		for(int i = 0; i < 20; i++)	{
			if(Next[nextIndex] == -1)	{
				System.out.printf("%4s", "");
				nextIndex++;
			}
			else	{
				System.out.printf("%4s", Next[nextIndex]);
				nextIndex++;
			}
		}
		//inputs and outputs from text files
		System.out.println("\n\nInput: \n" + input);
		System.out.println("Output:\n\n" + output);
	}
}