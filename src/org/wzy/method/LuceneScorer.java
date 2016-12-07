package org.wzy.method;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.wzy.meta.Question;
import org.wzy.tool.IOTool;
import org.wzy.tool.StringTool;

public class LuceneScorer implements ScoringInter{


	public QueryParser parser=null;
	public IndexSearcher searcher=null;
	public int parsingerror=0;
	public int maxpagesize=3;
	
	public boolean InitLucene(String indexdir)
	{
		IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexdir)));
			searcher = new IndexSearcher(reader);
			//Analyzer analyzer = new ComplexAnalyzer();
			//Analyzer analyzer=new SmartChineseAnalyzer();
			//Analyzer analyzer=new EnglishAnalyzer();
			//Analyzer analyzer=new StandardAnalyzer();
			Analyzer analyzer=new SimpleAnalyzer();
			String field = "contents";
			parser = new QueryParser(field, analyzer);
			parser.setAllowLeadingWildcard(true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public double ScoreByLucene(String searchfor,int maxPageSize)
	{
		try {
			Query query = parser.parse(searchfor);
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
		    return pageSize>0?score/pageSize:0;
		    
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
		InitLucene(indexdir);
	}
	@Override
	public double ScoreQAPair(Question qus,int aindex)
	{
		String querystr=qus.question_content+" "+qus.answers[aindex];
		double score=ScoreByLucene(querystr,maxpagesize);
		return score;
	}

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			
			//for question's content
			String question_content=q.question_content;
			//replace question tab at the beginning: 1 .
			String[] ss=question_content.split("[\\s]+");
			if(ss.length>2&&ss[0].matches("[0-9]+")&&(ss[1].equals(".")||ss[1].equals("-rrb-")))
			{
				question_content=StringTool.JoinStrings(ss, 2, " ");
			}
			//replace space line
			question_content=question_content.replaceAll("[_]+", "what");
			//replace number
			question_content=question_content.replaceAll("-?[0-9]+\\.?+[0-9]*", "value");
			//replace / to divide
			question_content=question_content.replaceAll("/", " divide ");
			q.question_content=question_content;
			
			//for answers' contents
			
			/*for(int j=0;j<q.answers.length;j++)
			{
				String answer=q.answers[j];
				
			}*/
		}
	}
	
	public static void main(String[] args)
	{
		List<Question> qList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\ck12_6000\\mcqa_12.06v.little.head100", "utf8");
		LuceneScorer ls=new LuceneScorer();
		ls.PreProcessingQuestions(qList);
	}
	
}
