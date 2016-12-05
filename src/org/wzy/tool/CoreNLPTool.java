package org.wzy.tool;


import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CoreNLPTool {

	
	public static CoreNLPTool UniqueObject;
	public static void CreateUniqueObject()
	{
		UniqueObject=new CoreNLPTool();
		//UniqueObject.InitTool();
	}
	
	
	
	
	public StanfordCoreNLP pipeline;
	public void InitTool()
	{
		 Properties props = new Properties();
		 props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner,");//" parse, dcoref, sentiment");
		 pipeline = new StanfordCoreNLP(props);
	}
	
	public void InitTool(String pipeProperties)
	{
		 Properties props = new Properties();
		 props.setProperty("annotators", pipeProperties);//" parse, dcoref, sentiment");
		 pipeline = new StanfordCoreNLP(props);		
	}
	
	public String[] Processing(String text)
	{
		Annotation annotation=new Annotation(text);
		pipeline.annotate(annotation);
		
		List<String> reslist=new ArrayList<String>();
		
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if(sentences!=null)
		{
			for(int i=0;i<sentences.size();i++)
			{
				 CoreMap sentence = sentences.get(i);
				 List<CoreLabel> tokenList=sentence.get(CoreAnnotations.TokensAnnotation.class);
				 for(int j=0;j<tokenList.size();j++)
				 {
					 CoreLabel token=tokenList.get(j);
					 
					 //System.out.println(token.keySet()); 
					 //String tokenstr=token.toShorterString();				 
					 String lemma=token.getString(CoreAnnotations.LemmaAnnotation.class);	 
					 reslist.add(lemma);
				 }
			}
		}
		
		return reslist.toArray(new String[0]);
	}
	
	
	public List<CoreMap> Processing2CoreMap(String text)
	{
		Annotation annotation=new Annotation(text);
		pipeline.annotate(annotation);
		
		List<String> reslist=new ArrayList<String>();
		
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		return sentences;
	}	
	
	
}
