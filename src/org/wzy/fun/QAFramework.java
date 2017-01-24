package org.wzy.fun;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.wzy.meta.*;
import org.wzy.method.KBModel;
import org.wzy.method.ScoringInter;
import org.wzy.method.TrainInter;
import org.wzy.method.scImpl.LuceneScorer;

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
	public TrainModel trainModel=null;
	
	//for wzy debug at 12.22
	public int has_path_question_count=0;
	
	
	public List<double[]> debug_scoreList=new ArrayList<double[]>(); 
	public int AnswerOneQuestion(Question q)
	{
		List<IndexAndScore> iasList=new ArrayList<IndexAndScore>();
		double[] debug_score_record=new double[q.answers.length];
		
		boolean has_path=false;
		q.scores=new double[q.answers.length];
		for(int i=0;i<q.answers.length;i++)
		{
			IndexAndScore ias=new IndexAndScore();
			ias.index=i;
			ias.score=scorer.ScoreQAPair(q, i);
			
			//for debug by wzy, at 12.22
			if(Math.abs(ias.score)>1e-6)
				has_path=true;
			
			iasList.add(ias);
			
			debug_score_record[i]=ias.score;
			q.scores[i]=ias.score;
		}
		if(predict_train)
			debug_scoreList.add(debug_score_record);
		
		Collections.sort(iasList,new IndexAndScore());
		
		if(has_path)
			has_path_question_count++;
		
		return iasList.get(0).index;
	}
	
	public int[] AnswerQuestions(List<Question> qList)
	{
		if(scorer==null)
		{
			System.err.println("There is no model to score answers.");
			System.exit(-1);
		}
		
		//scorer.PreProcessingQuestions(qList);
		
		int[] res=new int[qList.size()];
		for(int i=0;i<qList.size();i++)
		{
			res[i]=AnswerOneQuestion(qList.get(i));
		}
		
		System.err.println("[Debug] the number of questions at least with a path is "+has_path_question_count);
		
		return res;
	}
	
	
	
	public int[] GetAnswers(List<Question> qList)
	{
		int[] ans=new int[qList.size()];
		for(int i=0;i<qList.size();i++)
		{
			ans[i]=qList.get(i).AnswerKey;
		}
		return ans;
	}
	
	public int[] AnswerQuestionsMultiThread(List<Question> qList,int numThread)
	{
		long start=System.currentTimeMillis();
		ExecutorService exec = Executors.newFixedThreadPool(numThread); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		
		LuceneScorer MainLS=(LuceneScorer)scorer;
		
		LuceneScorer[] scoreThreads=new LuceneScorer[numThread];
		for(int i=0;i<scoreThreads.length;i++)
		{
			scoreThreads[i]=new LuceneScorer();
			//scoreThreads[i].save_paragraphs=true;	
			scoreThreads[i].threadid=i;
			scoreThreads[i].InitScorer(MainLS.paraMap);
			scoreThreads[i].qthreadList=new ArrayList<Question>();
			alThreads.add(scoreThreads[i]);
		}
		
		for(int i=0;i<qList.size();i++)
		{
			scoreThreads[i%numThread].qthreadList.add(qList.get(i));
		}
		System.out.println("question sum "+qList.size());
		
		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		exec.shutdown();
		
		int[] ans=new int[qList.size()];
		for(int i=0;i<qList.size();i++)
		{
			ans[i]=qList.get(i).predict_answer;
		}
		
		long end=System.currentTimeMillis();
		System.out.println("IR is over at "+(end-start)+"ms");
		
		return ans;
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
	
	
	
	///////////////////training//////////////////////////////
	public boolean L1regular=false;
	public boolean project=false;
	public boolean trainprintable=false;//true;	
	public Random rand=new Random();	
	public int Epoch=500;
	public int minibranchsize=64;
	public double gamma=1e-4;
	public double margin=1.;
	public int random_data_each_epoch=1000;
	public boolean bern=false;//true;//false;//true;
	public double lammadaL1=0.;
	public double lammadaL2=0;
	public String printMiddleModel_dir=null;
	public int printEpoch=100;
	//for debug
	public int errcount=0;
	public boolean quiet=false;
	public boolean predict_train=false;
	public String print_log_file=null;
	//public TrainInter trainInter;
	public String debug_score_record_dir="./";
	
	public void Training(List<Question> trainList,List<Question> validList)
	{
		//scorer.PreProcessingQuestions(trainList);
		Question[] traindatas=trainList.toArray(new Question[0]);
		
		int branch=traindatas.length/minibranchsize;
		//if(traindatas.length%minibranchsize>0) //if the size of minibranch didn't touch minibranch size
			//branch++;
		double lasttrain_point_err=Double.MAX_VALUE;
		double lasttrain_pair_err=Double.MAX_VALUE;				
		double lastvalid_point_err=Double.MAX_VALUE;
		double lastvalid_pair_err=Double.MAX_VALUE;	
		for(int epoch=0;epoch<Epoch;epoch++)
		{
			//Disrupt the order of training data set
			long start=System.currentTimeMillis();
			for(int i=0;i<random_data_each_epoch;i++)
			{
				int a=rand.nextInt(traindatas.length);
				int b=rand.nextInt(traindatas.length);	
				Question tmp=traindatas[a];
				traindatas[a]=traindatas[b];
				traindatas[b]=tmp;
			}
			
			for(int i=0;i<branch;i++)
			{
				int sindex=i*minibranchsize;
				int eindex=(i+1)*minibranchsize-1;
				if(eindex>=traindatas.length)
					eindex=traindatas.length-1;
				//trainInter.InitGradients();
				//change by wzy at 12.27
				scorer.InitAllGradients();
				OneBranchTraining(traindatas,sindex,eindex);	
			}
			long end=System.currentTimeMillis();
			
			if(predict_train)
			{
				int[] results=AnswerQuestions(trainList);
				int[] ans=GetAnswers(trainList);
				double score=Evluation(results, ans);
				
				if(debug_score_record_dir!=null)
				{
					PrintScoreList(debug_score_record_dir+epoch+"score");
				}
				debug_scoreList=new ArrayList<double[]>();
				
				results=AnswerQuestions(validList);
				ans=GetAnswers(validList);
				double validscore=Evluation(results, ans);
				
				System.out.println("Epoch "+epoch+" is end at "+(end-start)/1000+"s\t+rate is "+score+"\t"+validscore);
			}
			else
			{
				System.out.println("Epoch "+epoch+" is end at "+(end-start)/1000+"s");
			}
		}
	}
	
	public void PrintScoreList(String filename)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<debug_scoreList.size();i++)
			{
				double[] scores=debug_scoreList.get(i);
				for(int j=0;j<scores.length;j++)
				{
					ps.print(scores[j]+"\t");
				}
				ps.println();
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void OneBranchTraining(Question[] traindatas,int sindex,int eindex)
	{
		//List<Object> embeddingList=trainInter.ListingEmbedding();
		//List<Object> gradientList=trainInter.ListingGradient();
		for(int i=sindex;i<=eindex;i++)
		{
			//trainInter.CalculateGradient(traindatas[i]);
			scorer.CalLoss(traindatas[i]);
		}
		
		//UpgradeGradients(embeddingList,gradientList);
		//trainInter.UpgradeGradients(gamma);
		//changed by wzy at 2016.12.27
		scorer.UpgradeGradients(gamma);
		/*if(project)
			BallProjecting(embeddingList);
		else
			RegularEmbedding(traindatas,sindex,eindex);*/
	}
	
	
	public List<Question> RightAnsweringQuestions(List<Question> qList)
	{
		int[] results=AnswerQuestions(qList);
		int[] ans=GetAnswers(qList);
		List<Question> rightList=new ArrayList<Question>();
		for(int i=0;i<ans.length;i++)
		{
			if(results[i]==ans[i])
			{
				rightList.add(qList.get(i));
			}
		}
		return rightList;
	}
	

	
	
}
