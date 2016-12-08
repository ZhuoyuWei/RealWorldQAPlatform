package org.wzy.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.method.LSTMRepresentation;
import org.wzy.method.LuceneScorer;
import org.wzy.method.ScoringInter;
import org.wzy.method.SumRepresentation;
import org.wzy.method.TrainModel;
import org.wzy.method.WordEmbScorer;
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
	
	public static void TrainModel(QAFramework qaframe,Question[] train_questions)
	{
		TrainModel trainModel=new TrainModel();
		
		trainModel.scoreInter=qaframe.scorer;
		if(!trainModel.scoreInter.Trainable())
			return;
		trainModel.trainInter=trainModel.scoreInter.GetTrainInter();
		
		trainModel.Training(train_questions);
		
	}
	
	public static Map<String,String> ReadConfigureFile(String filename) throws IOException
	{
		Map<String,String> paraMap=new HashMap<String,String>();
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			paraMap.put(ss[0], ss[1]);
		}
		return paraMap;
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
		case 1:{
			qaframe.scorer=new WordEmbScorer();
			Map<String,String> paraMap=new HashMap<String,String>();
			paraMap.put("embfile", args[3]);
			paraMap.put("textModel", "lstm");
			
			
			
			
			break;
		}
		}
		
		//for train
		TrainModel(qaframe,questionList.toArray(new Question[0]));
		System.exit(-1);
		
		int[] results=qaframe.AnswerQuestions(questionList);
		int[] ans=GetAnswers(questionList);
		
		double score=qaframe.Evluation(results, ans);
		System.out.println("Correct rate is "+score);
	}
	
}
