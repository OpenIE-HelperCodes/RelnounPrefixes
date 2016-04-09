/**
 * 
 */
package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import domain.Sentence;

/**
 * @author harinder
 *
 */
public class TestMain {
	
	static final int THRESHOLD = 20;

	static List<String> relnouns = new ArrayList<String>();
	
	static String[] allPostagsArr = {"CC","CD","DT","EX","FW","IN","JJ","JJR","JJS","LS","MD","NN","NNS","NNP","NNPS","PDT","POS","PRP","PRP$","RB","RBR","RBS","RP","SYM","TO","UH","VB","VBD","VBG","VBN","VBP","VBZ","WDT","WP","WP$","WRB"};
	static String[] rejectPostagsArr = { ",", "IN", "PRP$", "DT", "CC", "CD", ":", "WRB", "TO", "POS", "-RRB-", "-LRB-", "O", "VB", "VBZ", "VBD", "WDT", "WP", "WP$", "MD", "RB", "RBR" };
	
	static List<String> allPostags = Arrays.asList(allPostagsArr);
	static List<String> rejectPostags = Arrays.asList(rejectPostagsArr);
	
	static POSModel model = new POSModelLoader().load(new File(
			"jars/en-pos-maxent.bin"));
	static POSTaggerME tagger = new POSTaggerME(model);
	
	//static Set<String> prefixesSet = new HashSet<String>();
	
	static Map<String, Integer> prefixCountMap = new HashMap<String, Integer>();
	static Map<String, Map<String, Integer>> prefixRelnounMap = new HashMap<String, Map<String, Integer>>();
	static Map<String, String> prefixSentenceMap = new HashMap<String, String>();
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		run();
		//printPrefixes();
	}
	
	/*private static void printPrefixes() throws IOException {
		PrintWriter pw = new PrintWriter(new File("data/final_prefixes"));
		
		for (String prefix : prefixesSet) {
			pw.println(prefix);
		}
		
		pw.close();
	}*/
	
	private static void printPrefixes() throws IOException {
		PrintWriter pw = new PrintWriter(new File("data/final_prefixes"));
		
		for (Entry<String, Integer> entry : prefixCountMap.entrySet()) {
			int count = entry.getValue();
			if(count < THRESHOLD) continue;
			
			String sentence = prefixSentenceMap.get(entry.getKey());
			
			StringBuilder relCountBuilder = new StringBuilder("");
			for (Entry<String, Integer> relCountEntry : prefixRelnounMap.get(entry.getKey()).entrySet()) {
				relCountBuilder.append(relCountEntry.getKey()+":"+relCountEntry.getValue()+", ");
			}
			
			
			pw.println(entry.getKey() + "||" + count + "||" + relCountBuilder + "||" + sentence);
		}
		
		pw.close();
	}
	
	private static void readRelnouns(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		
		String line = br.readLine();
		
		while(line!=null){
			line = line.toUpperCase();
			relnouns.add(line);
			
			line = br.readLine();
		}
		
		br.close();
		
	}
	
	private static Map<String, Boolean> readDemonyms() throws IOException {
		String demonymsPath = "data/demonyms.csv";
		BufferedReader br = new BufferedReader(new FileReader(new File(demonymsPath)));
		
		Map<String, Boolean> demonymsMap = new HashMap<String, Boolean>();
		
		String line = br.readLine();
		
		while(line!=null){
			line = line.toUpperCase();
			
			String[] lineArr = line.split(",");
			String[] lineArr_first = lineArr[0].split(" ");
			String[] lineArr_second = lineArr[1].split(" ");
			
			for (String str : lineArr_first) {
				demonymsMap.put(str, true);
			}
			
			for (String str : lineArr_second) {
				demonymsMap.put(str, true);
			}
			//demonymsMap.put(lineArr[0], true);
			//demonymsMap.put(lineArr[1], true);
			
			line = br.readLine();
		}
		
		br.close();
		
		return demonymsMap;
	}

	private static List<String> chunk(String input) throws IOException {
		
		ObjectStream<String> lineStream = new PlainTextByLineStream(
				new StringReader(input));

		String line;
		String whitespaceTokenizerLine[] = null;

		String[] tags = null;
		while ((line = lineStream.read()) != null) {
			whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
					.tokenize(line);
			tags = tagger.tag(whitespaceTokenizerLine);
		}

		// chunker
		InputStream is = new FileInputStream("jars/en-chunker.bin");
		ChunkerModel cModel = new ChunkerModel(is);

		ChunkerME chunkerME = new ChunkerME(cModel);
		String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);

		List<String> chunkInfo = new ArrayList<String>();
		for (String s : result)
			// System.out.println(s);
			chunkInfo.add(s);

		return chunkInfo;
	}
	
	
	
	
	private static void run() throws FileNotFoundException, IOException {
		Map<String, Boolean> demonymsMap = readDemonyms();
		readRelnouns("data/nouns_of.txt");//TODO
		readRelnouns("data/nouns.txt");//TODO
		//readRelnouns("data/temp");

		//List<Word_postagged> prefixes = new ArrayList<Word_postagged>();

		BufferedReader br = new BufferedReader(new FileReader(new File(
				"/home/harinder/Documents/IITD_MTP/Open_nre/HelperCodes/noToGit_RelnounPrefixes_postagged_npChunked_Sentences")));
		String line = br.readLine();
		while (line != null) {

			line = line.toUpperCase();
			// no need to process the line if relnoun not present
			if (!isRelnounPresent(line)) {
				line = br.readLine();
				continue;
			}

			String splitted[] = line.split(" \\.\t");

			for (int i = 0; i < splitted.length - 1; i += 2) {
				if (!isRelnounPresent(splitted[i])) {
					continue;
				}

				Sentence sentenceObj = new Sentence();
				sentenceObj.postag = splitted[i + 1].split(" ");
				int lenCurrSentence = sentenceObj.postag.length;

				splitted[i] = splitted[i].replace('\t', ' ');
				String[] sentenceArr = splitted[i].split(" ");

				int start = sentenceArr.length - lenCurrSentence;
				if (start < 0)
					continue; // something is wrong, let's continue
				List<String> finalSentence = new ArrayList<String>();

				for (int j = start; j < sentenceArr.length; j++) {
					finalSentence.add(sentenceArr[j]);
				}

				sentenceObj.sentence = finalSentence;

				String relnoun = getRelnounPresent(splitted[i]);
				int relnounIndex = finalSentence.indexOf(relnoun);

				if (relnounIndex <= 0)
					continue;
				
				// getting the chunkInfo
				List<String> chunkInfo = chunk(ListString_to_String(finalSentence));
				String relnounChunk = chunkInfo.get(relnounIndex);
				
				if(!relnounChunk.equals("I-NP")) continue; //there is no prefix

				int prefixIndex = relnounIndex - 1;

				String prefix = sentenceObj.sentence.get(prefixIndex);
				
				if(prefix.length()<=2) continue;
				if(prefix.contains("#")||prefix.contains("=")||prefix.contains("!")||prefix.contains("@")||prefix.contains("$")||prefix.contains("%")
						||prefix.contains("^")||prefix.contains("&")||prefix.contains("*")||prefix.contains("(")||prefix.contains(")")||prefix.contains("~")
						||prefix.contains("`")||prefix.contains("<")||prefix.contains(">")||prefix.contains("?")||prefix.contains(":")||prefix.contains(";")
						||prefix.contains("[")||prefix.contains("]")||prefix.contains("{")||prefix.contains("}")||prefix.contains("\\")||prefix.contains("|")
						||prefix.contains("'")||prefix.contains("/")||prefix.contains("\"")||prefix.contains("�")) continue;
				
				if(demonymsMap.get(prefix)!=null) continue;//prefix is a demonym, I don't need it
				
				if(prefix.endsWith("'S")||prefix.endsWith("`S")||prefix.endsWith("’S")) continue; //eg- Toshiba's is not what we are looking for
				
				String prefixPostag = sentenceObj.postag[prefixIndex];
				
				//reject if prefixChunk is B-NP and prefixPostag is NNP => less probability of being a prefix
				String prefixChunk = chunkInfo.get(prefixIndex);
				if(prefixChunk.equals("B-NP") && (prefixPostag.equals("NNP")||prefixPostag.equals("NN"))) 
					continue;

				if (rejectPostags.contains(prefixPostag)) continue;
				if (!allPostags.contains(prefixPostag)) continue;

				String finalSentence_str = ListString_to_String(finalSentence);
				
				String prefixWithPostag = prefix + "||" + prefixPostag; 
				
				Integer val_count = prefixCountMap.get(prefixWithPostag);
				int count = 0;
				if(val_count != null) count = val_count;
				
				prefixCountMap.put(prefixWithPostag, count+1);
				prefixSentenceMap.put(prefixWithPostag, relnoun+"||"+finalSentence_str);
				
				Map<String, Integer> relnounsCountMap = prefixRelnounMap.get(prefixWithPostag);
				if(relnounsCountMap == null) {relnounsCountMap = new HashMap<String, Integer>(); prefixRelnounMap.put(prefixWithPostag, relnounsCountMap);}
				Integer rel_count_val = relnounsCountMap.get(relnoun);
				int rel_count = 0;
				if(rel_count_val != null) rel_count = rel_count_val;
				relnounsCountMap.put(relnoun, rel_count+1);
				
				//System.out.println();//TODO-comment
				//System.out.println(finalSentence_str);//TODO-comment
				//System.out.println(prefixWithPostag);//TODO-comment

			}
			line = br.readLine();
		}

		printPrefixes();
		br.close();
	}

	private static String ListString_to_String(List<String> listString) {
		StringBuilder sb = new StringBuilder();

		for (String s : listString) {
			sb.append(s).append(' ');
		}

		sb.deleteCharAt(sb.length() - 1); // delete last space
		String newString = sb.toString();

		return newString;
	}

	private static String getRelnounPresent(String inputString) {
		for (int i = 0; i < relnouns.size(); i++) {
			if (inputString.contains(relnouns.get(i))) {
				return relnouns.get(i);
			}
		}
		return null;
	}

	private static boolean isRelnounPresent(String inputString) {
		String relnoun = getRelnounPresent(inputString);
		if (relnoun == null)
			return false;
		return true;
	}

}
