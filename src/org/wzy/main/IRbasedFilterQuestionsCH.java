package org.wzy.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.method.ScoringInter;
import org.wzy.method.scImpl.LuceneScorer;
import org.wzy.method.scImpl.PathCountScorer;
import org.wzy.method.scImpl.PathEmbScorer;
import org.wzy.method.scImpl.WordEmbScorer;
import org.wzy.method.trImpl.LSTMRepresentation;
import org.wzy.method.trImpl.SumRepresentation;
import org.wzy.tool.IOTool;

import com.lipiji.mllib.layers.MatIniter;

import org.wzy.fun.QAFramework;
import org.wzy.fun.TrainModel;
import org.wzy.meta.*;

public class IRbasedFilterQuestionsCH {

	public static String[] models={"LuceneScorer"};
	public static boolean debug_flag=true;
	

	
	public static void Training(QAFramework qaframe,List<Question> train_questions,List<Question> valid_questions)
	{
		//TrainModel trainModel=new TrainModel();
		
		if(!qaframe.scorer.Trainable())
			return;
		//qaframe.trainInter=qaframe.scorer.GetTrainInter();
		
		qaframe.Training(train_questions,valid_questions);
		
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
	
	public static List<Question>[] SplitIntoTrainAndValid(List<Question> qList,int rate)
	{
		List<Question>[] qusLists=new List[2];
		for(int i=0;i<2;i++)
			qusLists[i]=new ArrayList<Question>();
		
		for(int i=0;i<qList.size();i++)
		{
			if(i%rate==0)
			{
				qusLists[1].add(qList.get(i));
			}
			else
			{
				qusLists[0].add(qList.get(i));
			}
		}
		
		
		return qusLists;
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
		//List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[0], "utf8");
		//Chinese
		List<Question> questionList=IOTool.ReadXMLQuestion(args[0]);
		
		
		
		List<Question>[] qusLists=SplitIntoTrainAndValid(questionList,3);
		
		QAFramework qaframe=new QAFramework();
		int modelindex=Integer.parseInt(args[1]);
		//qaframe.scorer=(ScoringInter) Class.forName(models[modelindex]).newInstance();
		Map<String,String> paraMap=new HashMap<String,String>();
		switch(modelindex)
		{
		//lucene searching
		case 0:{ 
			qaframe.scorer=new LuceneScorer();		
			paraMap.put("indexdir", args[2]);
			
			paraMap.put("parser", args[3]);
			
			break;
		}
		//text inference
		case 1:{
			qaframe.scorer=new WordEmbScorer();
			//Map<String,String> paraMap=new HashMap<String,String>();
			paraMap.put("embfile", args[2]);
			//paraMap.put("textModel", "lstm");
			paraMap.put("textModel", "sum");
			
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
		
		case 2:
		{
			qaframe.scorer=new PathCountScorer();
			paraMap.put("entityFile", args[2]);
			paraMap.put("relationFile", args[3]);
			paraMap.put("factFile", args[4]);
			paraMap.put("entitylink", "max");
			paraMap.put("randomwalk", "exactly");

			if(debug_flag)
			{
				//paraMap.put("mid2name_file", args[5]);
				paraMap.put("logfile", args[5]);
			}
			
			
			break;
		}
		case 3:
		{
			qaframe.scorer=new PathEmbScorer();
			paraMap.put("entityFile", args[2]);
			paraMap.put("relationFile", args[3]);
			paraMap.put("factFile", args[4]);
			paraMap.put("entitylink", "ngram");
			paraMap.put("randomwalk", args[10]);

			if(debug_flag)
			{
				//paraMap.put("mid2name_file", args[5]);
				paraMap.put("logfile", args[5]);
			}
			
			paraMap.put("word_emb_file", args[6]);
			//paraMap.put("entity_emb_file",args[7]);	
			//paraMap.put("relation_emb_file",args[8]);
			
			
			paraMap.put("textModel", "sum");
			paraMap.put("pathModel", "sum");			
			
			int dim=Integer.parseInt(args[9]);
			paraMap.put("dim",dim+"");
			
			
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
		qaframe.scorer.PreProcessingQuestions(questionList);
		//for ai2 original dataset
		/*qusLists[0]=IOTool.ReadSimpleQuestionsCVS(args[11], "utf8");
		qusLists[1]=IOTool.ReadSimpleQuestionsCVS(args[12], "utf8");
		questionList.clear();
		questionList.addAll(qusLists[0]);
		questionList.addAll(qusLists[1]);
		System.out.println("Dataset describtion: train "+qusLists[0].size()+"\ttest "+qusLists[1].size());
		
		
		
		
		//for debug concept paths
		IOTool.PrintSimpleQuestionsWithConceptPaths(qusLists[0], args[11]+".concept."+args[10], "utf8");
		IOTool.PrintSimpleQuestionsWithConceptPaths(qusLists[1], args[12]+".concept."+args[10], "utf8");*/		
		
		//test read
		/*List<Question> tmp0=IOTool.ReadSimpleQuestionsCVSWithConceptPaths(args[11]+".withpath", "utf8");
		List<Question> tmp1=IOTool.ReadSimpleQuestionsCVSWithConceptPaths(args[12]+".withpath", "utf8");	
		for(int i=0;i<tmp0.size();i++)
		{
			if(tmp0.get(i).ans_paths.length!=qusLists[0].get(i).ans_paths.length)
			{
				System.err.println("debug error: wrong read concept paths");
				System.exit(-1);
			}
			for(int j=0;j<tmp0.get(i).ans_paths.length;j++)
			{
				if(tmp0.get(i).ans_paths[j].length!=qusLists[0].get(i).ans_paths[j].length)
				{
					System.err.println("debug error: wrong read concept paths");
					System.exit(-1);
				}
			}
		}*/
		
		
		
		//for train
		/*Training(qaframe,qusLists[0],qusLists[1]);
		System.exit(-1);
		*/
		
		int[] results=qaframe.AnswerQuestionsMultiThread(questionList,32);
		int[] ans=qaframe.GetAnswers(questionList);

		
		double score=qaframe.Evluation(results, ans);
		
		System.out.println("Correct rate is "+score);
		System.out.println("error parsing "+((LuceneScorer)qaframe.scorer).parsingerror);
		
		try {
			//IOTool.PrintWrongQuestionIdWithPredict(questionList, args[4], "utf8");
			IOTool.PrintQuestionsPredictScores(questionList, args[4], "utf8");
			//IOTool.PrintQuestionSnippetFromLucene(questionList, args[5], "utf8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
