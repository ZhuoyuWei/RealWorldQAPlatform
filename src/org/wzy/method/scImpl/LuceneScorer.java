package org.wzy.method.scImpl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.wzy.meta.Question;
import org.wzy.method.ScoringInter;
import org.wzy.method.TrainInter;
import org.wzy.tool.IOTool;
import org.wzy.tool.StringTool;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;

public class LuceneScorer implements ScoringInter,Callable{


	public QueryParser parser=null;
	public IndexSearcher searcher=null;
	public Analyzer analyzer=null;
	public int parsingerror=0;
	public int maxpagesize=3;
	
	
	//public Question qthread;
	public List<Question> qthreadList;
	public Map<String,String> paraMap;
	
	//obtain snippet
	public boolean save_paragraphs=false;
	public int threadid;
	
	public CharArraySet ReadStopwordFromFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		CharArraySet cas=new CharArraySet(5000,true);
		while((buffer=br.readLine())!=null)
		{
			cas.add(buffer);
		}
		return cas;
	}
	
	public boolean InitLucene(String indexdir,String parsertype)
	{
		IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexdir)));
			searcher = new IndexSearcher(reader);
			//Analyzer analyzer = new ComplexAnalyzer();
			//Analyzer analyzer=new SmartChineseAnalyzer();
			//Analyzer analyzer=new EnglishAnalyzer();
			//Analyzer analyzer=new StandardAnalyzer();
			//Analyzer analyzer=new SimpleAnalyzer();
			CharArraySet stopwordSet=ReadStopwordFromFile("../data/stopwords_cn.txt","utf8");
			//System.out.println(stopwordSet.size());
			//Reader stopwordReader=new BufferedReader(new InputStreamReader(new FileInputStream("../data/stopwords_cn.txt"),"utf8"));
			
			
			switch(parsertype)
			{
			case "english":
			{
				analyzer=new EnglishAnalyzer(stopwordSet);
				break;
			}
			case "complex":
			{
				analyzer=new ComplexAnalyzer();
				break;
			}
			case "standard":
				analyzer=new StandardAnalyzer();
				break;
			}
			String field = "contents";
			parser = new QueryParser(field, analyzer);
			parser.setAllowLeadingWildcard(true);
			


			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public double ScoreByLucene(String searchfor,int maxPageSize,Question question,int answerindex)
	{
		try {
			String tmp=QueryParser.escape(searchfor);
			//System.out.println(tmp);
			Query query = parser.parse(tmp);
			//QueryScorer scorer = new QueryScorer()));
			
			
			TopDocs results = searcher.search(query, maxPageSize);
		    ScoreDoc[] hits = results.scoreDocs;
		    
		    /*if(hits.length>0)
		    	return hits[0].score;
		    else
		    	return 0;*/
		    
		    int pageSize=hits.length<maxPageSize?hits.length:maxPageSize;
		    double score=0;
		    for(int i=0;i<pageSize;i++)
		    {
		    	
		    	score+=hits[i].score;
		    }
		    //return pageSize>0?score/pageSize:0;
		    
		   // System.out.println(score);
		    
			if(save_paragraphs)
			{
				//System.out.println("Snippet");
				QueryScorer scorer =new QueryScorer(query, "contents");
				Highlighter highlighter= new Highlighter(scorer);
				highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
				for(int i=0;i<pageSize;i++)
				{
					//System.out.println("Snippet "+i);
					String snippet=null;
					try {
						snippet = this.GetSnippet(hits[i], highlighter);
						System.out.println(snippet);
					} catch (InvalidTokenOffsetsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(snippet!=null)
					{
						question.paragraphs[answerindex]+=("\t"+snippet);
					}
				}
			}
		    
		    
		    return score;
		    
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			parsingerror++;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}	
	
	
	
	
	@Override
	public void InitScorer(Map<String,String> paraMap)
	{
		String indexdir=paraMap.get("indexdir");
		if(indexdir==null)
		{
			System.err.println("Init Lucene scorer fail.");
			System.exit(-1);
		}
		String parser=paraMap.get("parser");
		InitLucene(indexdir,parser);
		this.paraMap=paraMap;
	}
	@Override
	public double ScoreQAPair(Question qus,int aindex)
	{
		String querystr=qus.question_content+" "+qus.answers[aindex];
		double score=ScoreByLucene(querystr,maxpagesize,qus,aindex);
		return score;
	}

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		
		//strong replace
		try {
			this.removeAllStopLabel(qList);
			return;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			
			//for question's content
			/*String question_content=q.question_content;
			//replace question tab at the beginning: 1 .
			String[] ss=question_content.split("[\\s]+");
			if(ss.length>2&&ss[0].matches("[0-9]+")&&(ss[1].equals(".")||ss[1].equals("-rrb-")))
			{
				question_content=StringTool.JoinStrings(ss, 2, " ");
			}
			//replace space line
			question_content=question_content.replaceAll("[_]+", "what");
			//replace number
			question_content=question_content.replaceAll("-?[0-9]+[\\.,]?+[0-9]*", "value");
			//replace / to divide
			question_content=question_content.replaceAll("/", " divide ");*/
			q.question_content=q.question_content.replaceAll("/", " divide ");
			
			//for answers' contents
			
			for(int j=0;j<q.answers.length;j++)
			{
				//String answer=q.answers[j];
				q.answers[j]=q.answers[j].replaceAll("/", " divide ");
			}
		}
	}
	
	public void removeAllStopLabel(List<Question> qList) throws FileNotFoundException
	{
		long start=System.currentTimeMillis();
		List<String> stopList=new ArrayList<String>();
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("../data/stopwords_label.txt"),"utf8"));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<1)
					continue;
				stopList.add(buffer.trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int t=0;t<stopList.size();t++)
		{
			String stoptoken=stopList.get(t);
			for(int i=0;i<qList.size();i++)
			{
				Question q=qList.get(i);
				q.question_content=q.question_content.replace(stoptoken, "");
				
				for(int j=0;j<q.answers.length;j++)
				{
					q.answers[j]=q.answers[j].replace(stoptoken, "");
				}
			}
		}
		long end =System.currentTimeMillis();
		System.out.println("pre process question takes "+(end-start)+" ms");
	}

	
	
	public static void main(String[] args)
	{
		List<Question> qList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\ck12_6000\\mcqa_12.06v.little.head100", "utf8");
		LuceneScorer ls=new LuceneScorer();
		ls.PreProcessingQuestions(qList);
	}

	@Override
	public double CalLoss(Question q) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean Trainable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TrainInter GetTrainInter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void InitAllGradients() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void InitPathWeightRandomly(List<Question> questionList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		if(qthreadList==null)
			return null;
		try{
		System.out.println("thread "+threadid+" is start with "+qthreadList.size()+" questions");
		for(int i=0;i<qthreadList.size();i++)
		{
			Question qthread=qthreadList.get(i);
			qthread.predict_answer=AnswerOneQuestion(qthread);
		}}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public int AnswerOneQuestion(Question q)
	{
		if(save_paragraphs)
		{
			q.paragraphs=new String[q.answers.length];
			for(int i=0;i<q.paragraphs.length;i++)
			{
				q.paragraphs[i]="";
			}
		}
		List<IndexAndScore> iasList=new ArrayList<IndexAndScore>();
		q.scores=new double[q.answers.length];
		for(int i=0;i<q.answers.length;i++)
		{			
			IndexAndScore ias=new IndexAndScore();
			ias.index=i;
			ias.score=ScoreQAPair(q, i);
			q.scores[i]=ias.score;
			iasList.add(ias);			
		}
		Collections.sort(iasList,new IndexAndScore());
		return iasList.get(0).index;
	}
	
	public String GetSnippet(ScoreDoc hit,Highlighter highlighter) throws IOException, InvalidTokenOffsetsException
	{
		Document document=null;
		document = searcher.doc(hit.doc);

		String content = document.get("contents");
		//System.out.println("************"+content);
		String snippet=null;
		//TokenStream tokenStream = TokenSources.getAnyTokenStream(
		//searcher.getIndexReader(), hit.doc, "contents", document, analyzer);
		//snippet=highlighter.getBestFragment(tokenStream, content);
		TokenStream tokenStream=analyzer.tokenStream("contents", new StringReader(content));
		snippet=highlighter.getBestFragment(tokenStream,content);


		System.out.println(snippet);
		
		return snippet;
	}
	
}


class IndexAndScore implements Comparator
{
	public int index;
	public double score;
	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		IndexAndScore i0=(IndexAndScore)arg0;
		IndexAndScore i1=(IndexAndScore)arg1;
		
		if(Math.abs(i0.score-i1.score)<1e-10)
			return 0;
		else if(i0.score>i1.score)
			return -1;
		else
			return 1;
	}
};
