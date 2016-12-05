package org.wzy.tool;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;

public class ParserTool {

	public static ParserTool UniqueObject;
	public static void CreateUniqueObject()
	{
		UniqueObject=new ParserTool();
		UniqueObject.InitParser();
	}
	
	
	/////////////////////////////////////
	
	 String modelPath = DependencyParser.DEFAULT_MODEL;
	 String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
	 MaxentTagger tagger;
	 DependencyParser parser;
	
	public void InitParser()
	{
		tagger = new MaxentTagger(taggerPath);
		parser = DependencyParser.loadFromModelFile(modelPath);
	}
	
	

	public List<GrammaticalStructure> DepandencyParserMultiSentences(String text)
	{
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		List<GrammaticalStructure> gsList=new ArrayList<GrammaticalStructure>();
		for (List<HasWord> sentence : tokenizer)
		{
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
		    GrammaticalStructure gs = parser.predict(tagged);
		    gsList.add(gs);
	    }
		return gsList;
	}
	
	
	
}
