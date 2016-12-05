package org.wzy.ir;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
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
import org.wzy.meta.*;
import org.wzy.pre.ReadXMLQuestion;
import org.wzy.tool.IOTool;

class AnsScore implements Comparator
{
	double score;
	int index;
	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		AnsScore a0=(AnsScore) arg0;
		AnsScore a1=(AnsScore) arg1;
		
		if(Math.abs(a0.score-a1.score)<1e-6)
		{
			return 0;
		}
		else if(a0.score>a1.score)
			return -1;
		else
			return 1;
		
	}
}



public class LuceneAllQuestion {
	
	public List<Question> wrongList=new ArrayList<Question>();
	public List<double[]> scoreList=new ArrayList<double[]>();
	public QueryParser parser=null;
	public IndexSearcher searcher=null;
	public int parsingerror=0;
	
	public boolean InitLucene(String indexdir)
	{
		IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexdir)));
			searcher = new IndexSearcher(reader);
			//Analyzer analyzer = new ComplexAnalyzer();
			//Analyzer analyzer=new SmartChineseAnalyzer();
			Analyzer analyzer=new StandardAnalyzer();
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
	public String PreForSegment(String s)
	{
		s=s.replaceAll("[\\s]+", "");
		s=s.replaceAll("[����\\(\\)]+", "");
		return s;
	}
	public boolean CheckByIR(Question q,int maxPageSize)
	{
		List<AnsScore> ansList=new ArrayList<AnsScore>(q.answers.length);
		double[] fordebug=new double[q.answers.length];
		for(int i=0;i<q.answers.length;i++)
		{
			String searchfor=q.question_content+" "+q.answers[i];
			//searchfor=PreForSegment(searchfor);
			AnsScore as=new AnsScore();
			as.index=i;
			as.score=ScoreByLucene(searchfor,maxPageSize);
			ansList.add(as);
			fordebug[i]=as.score;
		}
		Collections.sort(ansList,new AnsScore());
		scoreList.add(fordebug);
		return ansList.get(0).index==q.AnswerKey;
	}
	
	public double ScoreByLucene(String searchfor)
	{
		try {
			Query query = parser.parse(searchfor);
			TopDocs results = searcher.search(query, 1);
		    ScoreDoc[] hits = results.scoreDocs;
		    if(hits.length>0)
		    	return hits[0].score;
		    else
		    	return 0;
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
	
	
	public void IR_AllQuestion(List<Question> qList,int maxPageSize)
	{
		int[] answers=new int[qList.size()];
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			boolean flag=CheckByIR(q,maxPageSize);
			if(!flag)
			{
				wrongList.add(q);
			}
		}
		System.out.println("wrong questions size: "+wrongList.size()+"\trate "+((double)wrongList.size()/qList.size()));
	}
	
	public void DebugPrintScores(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<scoreList.size();i++)
		{
			for(int j=0;j<scoreList.get(i).length;j++)
			{
				ps.print(scoreList.get(i)[j]+"\t");
			}
			ps.println();
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		ReadXMLQuestion rxq=new ReadXMLQuestion();
		//List<Question> questionList=rxq.ReadXMLQuestion(args[0]);
		List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[0], "utf8");

		LuceneAllQuestion laq=new LuceneAllQuestion();
		laq.InitLucene(args[1]);
		long start=System.currentTimeMillis();
		laq.IR_AllQuestion(questionList,3);
		long end=System.currentTimeMillis();
		System.out.println("search all question in "+(end-start)+" ms");
		//PrintStream ps=new PrintStream(args[2]);
		//IOTool.PrintSimpleQuestions(questionList, ps);
		//ps.close();
		IOTool.PrintSimpleQuestions(questionList, args[2], "utf8");
		
		laq.DebugPrintScores(args[3]);
		System.out.println("parsering error: "+laq.parsingerror);
		
	}
}
