/*******************************************************************************
 * Copyright 2019 Mountain Fog, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package ai.idylnlp.nlp.text.metrics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.neovisionaries.i18n.LanguageCode;
import ai.idylnlp.model.exceptions.ModelLoaderException;
import ai.idylnlp.model.nlp.SentenceDetector;
import ai.idylnlp.model.nlp.Tokenizer;
import ai.idylnlp.nlp.sentence.SimpleSentenceDetector;
import ai.idylnlp.nlp.text.metrics.model.TextMetricsResult;
import ai.idylnlp.nlp.tokenizers.ModelTokenizer;
import ai.idylnlp.nlp.tokenizers.WhitespaceTokenizer;
import ai.idylnlp.nlp.utils.ngrams.NgramUtils;
import eu.crydee.syllablecounter.SyllableCounter;

/**
 * Calculates various metrics of text.
 * 
 * This class builds as a runnable jar that can be executed on
 * a given file. Each line in the file must be a separate
 * document. Each line (document) will be processed
 * individually.
 * 
 * @author Mountain Fog, Inc.
 *
 */
public class TextMetrics {
	
	private static final Logger LOGGER = LogManager.getLogger(TextMetrics.class);
	
	private SentenceDetector sentenceDetector;
	private Tokenizer tokenizer;
	
	public static void main(String[] args) throws ParseException, IOException, ModelLoaderException {
	  
	  LOGGER.info("Running non-interactively.");
	  
	  Options options = new Options();
	  options.addOption("f", true, "Full path to file to process line by line.");
	  options.addOption("s", true, "Full path to sentence model manifest.");
	  options.addOption("t", true, "Full path to token model manifest.");
	  options.addOption("o", true, "The output file.");
	  options.addOption("n", true, "The size of n-grams.");
	  
	  CommandLineParser parser = new DefaultParser();
	  CommandLine cmd = parser.parse(options, args);
	  
	  if(cmd.hasOption("f")) {
	  
	    final String fileToProcess = cmd.getOptionValue("f");
	    final File f = new File(fileToProcess);
	    	      
	    SentenceDetector sentenceDetector = null;
	      
	    // Get the sentence detector.
	    if(cmd.hasOption("s")) {
	      // TODO: Load the sentence model detector.
	    } else {
	      sentenceDetector = new SimpleSentenceDetector();
	    }
	      
	    Tokenizer tokenizer = null;
	      
	    // Get the tokenizer.
        if(cmd.hasOption("t")) {
          final String tokenModel = cmd.getOptionValue("t");
          tokenizer = new ModelTokenizer(new FileInputStream(tokenModel), LanguageCode.en);
        } else {
          tokenizer = WhitespaceTokenizer.INSTANCE;
        }
        
	    int ngramLength = 2;
	      
	    if(cmd.hasOption("n")) {
	      ngramLength = Integer.valueOf(cmd.getOptionValue("n"));
	    }
	    
	    File out = null;
	      
	    if(cmd.hasOption("o")) {
	      out = new File(cmd.getOptionValue("o"));
	    } else {
	      out = File.createTempFile("idylnlp", ".csv");
	    }
		FileOutputStream fos = new FileOutputStream(out);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	    bw.write("filename,unique-words,total-words,characters,sentences,max-sentence,avg-sentence,syllables,grade-level\n");
	    
	    Map<String, Integer> ngrams = new HashMap<>();
	    
	    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
	        String line;
	        while ((line = br.readLine()) != null) {

	        	TextMetrics textMetrics = new TextMetrics(sentenceDetector, tokenizer);

	        	TextMetricsResult r = textMetrics.calculate(line, ngramLength);

	        	bw.write(String.format("\"%s\",%s,%s,%s,%s,%s,%s,%s,%s\n", 
	        			f.getName(), r.getUniqueWords(), r.getTotalWords(), r.getCharacterCount(), r.getTotalSentences(),
	        			r.getMaxSentenceLength(), r.getAvgSentenceLength(), r.getTotalSyllables(), r.getFleschKincaidGradeLevel()));

	        	ngrams.putAll(r.getNgrams());

	        }
	    }

	    bw.close();
	    fos.close();
	      
	    LOGGER.info("Results written to: " + out.getAbsolutePath());
	      
	    Map<String, Integer> sorted = ngrams
	          .entrySet()
	          .stream()
	          .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	          .collect(
	              Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
	                  LinkedHashMap::new));
	      
	    for(String s : sorted.keySet()) {
	      FileUtils.write(new File(out.getAbsolutePath() + ".ngrams"), ngrams.get(s) + "\t" + s + "\n", true);	        
	    }
 
	    LOGGER.info("Wrote ngrams to: " + out.getAbsolutePath() + ".ngrams");

	  } else {

	    LOGGER.error("Missing directory. Specify directory to process with -d argument.");

	  }

	}

	/**
	 * Creates a new instance.
	 * @param sentenceDetector The {@link SentenceDetector} for extracting sentences
	 * from the text.
	 * @param tokenizer The {@link Tokenizer} for extracting tokens
	 * from the text.
	 */
	public TextMetrics(SentenceDetector sentenceDetector, Tokenizer tokenizer) {
		
		this.sentenceDetector = sentenceDetector;
		this.tokenizer = tokenizer;

	}
	
	   /**
     * Calculates metrics of text using token N-gram size of 2.
     * @param text The text.
     * @return A {@link TextMetricsResult} containing the metrics.
     */
    public TextMetricsResult calculate(String text) {
      
      return calculate(text, 2);
      
    }

	/**
	 * Calculates metrics of text.
	 * @param text The text.
	 * @param ngramLength The length of the token N-grams.
	 * @return A {@link TextMetricsResult} containing the metrics.
	 */
	public TextMetricsResult calculate(String text, int ngramLength) {
		
		SummaryStatistics stats = new SummaryStatistics();
		
		SyllableCounter sc = new SyllableCounter();
		
		Set<String> uniqueWords = new LinkedHashSet<>();
		Map<String, Integer> ngrams = new HashMap<>();
		
		int tokenCount = 0;
		int maxSentenceLength = 0;
		int totalSyllables = 0;
		
		String[] sentences = sentenceDetector.sentDetect(text);
		
		for(String sentence : sentences) {
			
			String[] tokens = tokenizer.tokenize(sentence);

			for(String token : tokens) {
				
				uniqueWords.add(token);
				
				totalSyllables += sc.count(token);
				
			}
			
			stats.addValue(tokens.length);
			
			tokenCount += tokens.length;
			
			if(sentence.length() > maxSentenceLength) {
				maxSentenceLength = sentence.length();
			}		
						
			final String[] ng = NgramUtils.getNgrams(tokens, ngramLength);
			
			for(String n : ng) {
			  
			  int count = ngrams.getOrDefault(n, 0) + 1;
			  ngrams.put(n, count);
			  
			}
			
		}
		
		TextMetricsResult result = new TextMetricsResult(uniqueWords.size(), tokenCount, text.length(), sentences.length,
				maxSentenceLength, stats.getMean(), totalSyllables, ngrams);
		
		return result;
		
	}
	
}
