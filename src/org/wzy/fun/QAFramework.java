package org.wzy.fun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.wzy.meta.*;
import org.wzy.method.ScoringInter;

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

public class QAFramework {

	public ScoringInter scorer=null;
	
	
	public int AnswerOneQuestion(Question q)
	{
		List<IndexAndScore> iasList=new ArrayList<IndexAndScore>();
		for(int i=0;i<q.answers.length;i++)
		{
			IndexAndScore ias=new IndexAndScore();
			ias.index=i;
			ias.score=scorer.ScoreQAPair(q, i);
			iasList.add(ias);
		}
		
		Collections.sort(iasList,new IndexAndScore());
		
		return iasList.get(0).index;
	}
	
	public int[] AnswerQuestions(List<Question> qList)
	{
		if(scorer==null)
		{
			System.err.println("There is no model to score answers.");
			System.exit(-1);
		}
		
		scorer.PreProcessingQuestions(qList);
		
		int[] res=new int[qList.size()];
		for(int i=0;i<qList.size();i++)
		{
			res[i]=AnswerOneQuestion(qList.get(i));
		}
		return res;
	}
	
	public double Evluation(int[] results,int[] answers)
	{
		if(results.length!=answers.length)
		{
			System.err.println("result list and answer list have different lengths");
			System.exit(-1);
		}
		int count=0;
		for(int i=0;i<results.length;i++)
		{
			if(results[i]==answers[i])
			{
				count++;
			}
		}
		
		return results.length>0?(count/(double)results.length):0;
	}
	
	
	
}
