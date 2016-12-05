package org.wzy.method;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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
			Analyzer analyzer=new EnglishAnalyzer();
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
	
	
}
