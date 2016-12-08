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
import org.wzy.method.WordEmbScorer;
import org.wzy.tool.IOTool;

import com.lipiji.mllib.layers.MatIniter;

import org.wzy.fun.QAFramework;
import org.wzy.fun.TrainModel;
import org.wzy.meta.*;

public class QAExperimentPlatform {

	public static String[] models={"LuceneScorer"};
	
	

	
	public static void Training(QAFramework qaframe,List<Question> train_questions)
	{
		//TrainModel trainModel=new TrainModel();
		
		if(!qaframe.scorer.Trainable())
			return;
		qaframe.trainInter=qaframe.scorer.GetTrainInter();
		
		qaframe.Training(train_questions);
		
		//wzy_debug
		((WordEmbScorer)qaframe.scorer).wzy_debug_ZeroPrint();
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
		//local
		/*args=new String[4];
		args[0]="D:\\KBQA\\DataSet\\ck12_6000\\mcqa_12.06v.4can";
		args[1]="1";
		args[2]="D:\\KBQA\\DataDump\\wordembeddings\\glove.6B\\glove.6B.50d.txt";
		args[3]="50";*/
		
		//question for test
		List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[0], "utf8");
		
		QAFramework qaframe=new QAFramework();
		int modelindex=Integer.parseInt(args[1]);
		//qaframe.scorer=(ScoringInter) Class.forName(models[modelindex]).newInstance();
		Map<String,String> paraMap=new HashMap<String,String>();
		switch(modelindex)
		{
		case 0:{
			qaframe.scorer=new LuceneScorer();
			
			paraMap.put("indexdir", args[2]);
			
			break;
		}
		case 1:{
			qaframe.scorer=new WordEmbScorer();
			//Map<String,String> paraMap=new HashMap<String,String>();
			paraMap.put("embfile", args[2]);
			paraMap.put("textModel", "lstm");
			
			int dim=Integer.parseInt(args[3]);
			paraMap.put("dim",args[3]);
			
			
			paraMap.put("inSize", dim+"");
			paraMap.put("outSize", dim+"");
			
			paraMap.put("typeLabel","1");
		
			paraMap.put("scale","0.1");
			paraMap.put("miu","0");
			paraMap.put("sigma","1");
			
			
			break;
		}
		}
		qaframe.scorer.InitScorer(paraMap);
		//for train
		Training(qaframe,questionList);
		
		System.exit(-1);
		
		int[] results=qaframe.AnswerQuestions(questionList);
		int[] ans=qaframe.GetAnswers(questionList);
		
		double score=qaframe.Evluation(results, ans);
		System.out.println("Correct rate is "+score);
	}
	
}
