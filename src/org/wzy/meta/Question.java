package org.wzy.meta;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wzy.tool.CoreNLPTool;
import org.wzy.tool.ParserTool;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

public class Question {

	//original information
	public String questionID;
	public String originalQuestionID;
	public int totalPossiblePoint;
	public int AnswerKey;
	public boolean isMultipleChoiceQuestion;
	public boolean includesDiagram;
	public String examName;
	public int schoolGrade;
	public int year;
	public String ori_question;
	public String subject;
	public String category;
	
	//Question and Answer
	public String question_content;
	public String[] answers;
	
	public List<CoreMap> question_stan_sentences;
	public List<GrammaticalStructure> question_de_structure;
	public List<CoreMap>[] answer_stan_sentences;
	public List<GrammaticalStructure>[] answer_de_structures;
	
	//For cluster features
	public String bagofword_feature_string=null;
	
	////////////////////Method///////////////////////////
	public void AI2ParsingString(String content)
	{
		//93885,
		//29398,
		//1,
		//C,
		//1,
		//0,
		//MCAS,
		//5,
		//2013,
		//A bird has just hatched from an egg. Which of the following stages most likely comes next in the life cycle of the bird? (A) birth (B) death (C) growth (D) reproduction,
		//Science,
		//Train
		
		String pattern="\"(.*)\"";
		Pattern p=Pattern.compile(pattern);
		Matcher matcher=p.matcher(content);
		if(matcher.find())
		{
			ori_question=matcher.group(1);
			//content.rep
		}
		
		
		String[] ss=content.split(",");
		if(ss.length<12)
		{
			System.err.println("question line parsing error");
			System.err.println(content);
			//System.err.println(ori_question);
			System.exit(-1);
		}
		questionID=ss[0];
		originalQuestionID=ss[1];
		totalPossiblePoint=Integer.parseInt(ss[2]);
		switch(ss[3])
		{
		case "A":
		{
			AnswerKey=0;
			break;
		}
		case "B":
		{
			AnswerKey=1;
			break;			
		}
		case "C":
		{
			AnswerKey=2;
			break;				
		}
		case "D":
		{
			AnswerKey=2;
			break;			
		}
		
		}
		
		isMultipleChoiceQuestion=ss[4].equals("1");
		includesDiagram=ss[5].equals("1");
		examName=ss[6];
		schoolGrade=Integer.parseInt(ss[7]);
		year=Integer.parseInt(ss[8]);
		if(ori_question==null)
			ori_question=ss[9];
		subject=ss[ss.length-2];
		category=ss[ss.length-1];
		
		//parse question content
		String[] tmpss=ori_question.split("\\([A-D]+\\)");
		if(tmpss.length<2)
		{
			System.err.println("question content parsing error");
			System.err.println(ori_question);
			System.err.println(content);
			System.exit(-2);
		}
		question_content=tmpss[0];
		answers=new String[tmpss.length-1];
		for(int i=1;i<tmpss.length;i++)
		{
			answers[i-1]=tmpss[i];
		}
		
	}
	
	
	public void DependencyParsing()
	{
		//question content
		if(question_content!=null)
		{
			question_de_structure=ParserTool.UniqueObject.DepandencyParserMultiSentences(question_content);
		}
		
		if(answers!=null)
		{
			answer_de_structures=new List[answers.length];
			for(int i=0;i<answers.length;i++)
			{
				answer_de_structures[i]=ParserTool.UniqueObject.DepandencyParserMultiSentences(answers[i]);
			}
		}
	}
	
	public void CoreNLPProcessing()
	{
		//question content
		if(question_content!=null)
		{
			question_stan_sentences=CoreNLPTool.UniqueObject.Processing2CoreMap(question_content);
		}
		
		if(answers!=null)
		{
			answer_stan_sentences=new List[answers.length];
			for(int i=0;i<answers.length;i++)
			{
				answer_stan_sentences[i]=CoreNLPTool.UniqueObject.Processing2CoreMap(answers[i]);
			}
		}
	}	
	
	/*@Override
	public String toString()
	{
		return "";
	}*/
	
	public String GetGrammerString()
	{
		StringBuilder sb=new StringBuilder();
		
		//Dependency tree
		sb.append("<depen>");
		if(question_stan_sentences!=null)
		{
			sb.append("<ques>");
			for(int i=0;i<question_stan_sentences.size();i++)
			{
				CoreMap sentence=question_stan_sentences.get(i);
				//SemanticGraph sg=sentence.get(BasicDependenciesAnnotation.class);
				//Collection<TypedDependency> collect_td=sg.typedDependencies();
				List<CoreLabel> labelList=sentence.get(CoreAnnotations.TokensAnnotation.class);
				
				for(int j=0;j<labelList.size();j++)
				{
					CoreLabel cl=labelList.get(j);
					String wordtag=cl.tag();
					System.out.println(cl.lemma()+"\t"+wordtag);
				}
	

				//sg.
				//String tmp=GrammaticalStructure.dependenciesToCoNLLXString(collect_td, sentence);
				//Tree tree=
				//sb.append("<item>");
				//sb.append(tmp);
				
				sb.append("</item>");				
			}
			sb.append("</ques>");
		}
		sb.append("</depen>");
		
		return sb.toString();
	}
	
	public void SerializGrammerToStream(ObjectOutputStream objOut)
	{
		for(int i=0;i<question_stan_sentences.size();i++)
		{
			CoreMap sentence=question_stan_sentences.get(i);
			try {
				objOut.writeObject(sentence);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
