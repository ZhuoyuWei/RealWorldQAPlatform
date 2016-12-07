package org.wzy.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.meta.Question;
import org.wzy.method.LuceneScorer;
import org.wzy.method.ScoringInter;
import org.wzy.tool.IOTool;
import org.wzy.fun.QAFramework;
import org.wzy.meta.*;

public class QAExperimentPlatform {

	public static String[] models={"LuceneScorer"};
	
	
	public static int[] GetAnswers(List<Question> qList)
	{
		int[] ans=new int[qList.size()];
		for(int i=0;i<qList.size();i++)
		{
			ans[i]=qList.get(i).AnswerKey;
		}
		return ans;
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		//question for test
		List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[0], "utf8");
		
		QAFramework qaframe=new QAFramework();
		int modelindex=Integer.parseInt(args[1]);
		//qaframe.scorer=(ScoringInter) Class.forName(models[modelindex]).newInstance();
		switch(modelindex)
		{
		case 0:{
			qaframe.scorer=new LuceneScorer();
			Map<String,String> paraMap=new HashMap<String,String>();
			paraMap.put("indexdir", args[2]);
			qaframe.scorer.InitScorer(paraMap);
			break;
		}
		}
		
		int[] results=qaframe.AnswerQuestions(questionList);
		int[] ans=GetAnswers(questionList);
		
		double score=qaframe.Evluation(results, ans);
		System.out.println("Correct rate is "+score);
	}
	
}
