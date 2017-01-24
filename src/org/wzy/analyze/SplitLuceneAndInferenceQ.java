package org.wzy.analyze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wzy.meta.Question;
import org.wzy.tool.IOTool;

class ScoreQuestion implements Comparator
{
	public Question q;
	public double score;
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		ScoreQuestion s1=(ScoreQuestion)o1;
		ScoreQuestion s2=(ScoreQuestion)o2;
		
		if(Math.abs(s1.score-s2.score)<1e-10)
			return 0;
		else if(s1.score<s2.score)
			return -1;
		else 
			return 1;
	}
}

public class SplitLuceneAndInferenceQ {

	
	public Map<String,double[]> qid2scores=new HashMap<String,double[]>();
	
	public void ReadScores(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=5)
				continue;
			double[] scores=new double[4];
			for(int i=0;i<4;i++)
			{
				scores[i]=Double.parseDouble(ss[i+1]);
			}
			qid2scores.put(ss[0], scores);
		}
		br.close();
	}
	
	public List<Question> SelectQuestionsForInference(List<Question> qList)
	{
		List<ScoreQuestion> sqList=new ArrayList<ScoreQuestion>();
		int errorcount=0;
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			double[] scores=qid2scores.get(q.questionID);
			ScoreQuestion sq=new ScoreQuestion();
			sq.q=q;
			if(scores!=null)
			{

				int maxindex=0;
				double maxscore=0.;
				
				for(int j=0;j<scores.length;j++)
				{
					if(maxscore<scores[j])
					{
						maxindex=j;
						maxscore=scores[j];
					}
				}
				
				if(maxindex!=q.AnswerKey)
				{
					sq.score=-1;
					errorcount++;
				}
				else
				{
					int secondindex=0;
					double secondscore=0.;
					for(int j=0;j<scores.length;j++)
					{
						if(j==maxindex)
							continue;
						if(secondscore<scores[j])
						{
							secondindex=j;
							secondscore=scores[j];
						}
					}
					sq.score=(maxscore-secondscore);
					if(secondscore>1e-6)
						sq.score/=secondscore;
				}
			}
			else
			{
				errorcount++;
				sq.score=-1;
			}
			sqList.add(sq);
			
			
			
			
		}
		
		System.out.println("wrong question is "+errorcount);
		
		Collections.sort(sqList,new ScoreQuestion());
		int selectcount=(int)(errorcount*1.333333);
		Set<String> qidSet=new HashSet<String>();
		for(int i=0;i<selectcount;i++)
		{
			qidSet.add(sqList.get(i).q.questionID);
		}
		
		
		
		List<Question> resList=new ArrayList<Question>();
		for(int i=0;i<qList.size();i++)
		{
			if(qidSet.contains(qList.get(i).questionID))
			{
				resList.add(qList.get(i));
			}
		}
		
		
		
		return resList;
		
	}
	
	public static void main(String[] args) throws IOException
	{
		List<Question> qallList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\life-science\\life-science_qa_12.22v_source.4can", "utf8");
		SplitLuceneAndInferenceQ sai=new SplitLuceneAndInferenceQ();
		sai.ReadScores("D:\\KBQA\\DataSet\\ck12_6000\\lucene_scores_allquestion_1.13v.scores");
		List<Question> infList=sai.SelectQuestionsForInference(qallList);
		IOTool.PrintSimpleQuestions(infList, "D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\life-science\\life-science_inf_all_1.14v.csv", "utf8");
	}
	
}
