package org.wzy.method.scImpl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.wzy.meta.NNParameter;
import org.wzy.meta.Question;
import org.wzy.method.ScoringInter;
import org.wzy.method.TextRepresentInter;
import org.wzy.method.TrainInter;
import org.wzy.method.trImpl.GRURepresentation;
import org.wzy.method.trImpl.LSTMRepresentation;
import org.wzy.method.trImpl.SumRepresentation;
import org.wzy.tool.MatrixTool;

public class TransEScorer implements ScoringInter{

	public Random rand=new Random();

	
	public int wordsize;
	public int dim;
	public TextRepresentInter textpre;
	public double pairwise_margin=1;
	
	public int normtype=2;

	@Override
	public void InitScorer(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		dim=Integer.parseInt(paraMap.get("dim"));
	
		String textModel=paraMap.get("textModel");
		NNParameter nnpara=new NNParameter();
		switch(textModel)
		{
		case "sum":
		{
			textpre=new SumRepresentation();
			break;
		}
		case "lstm":
		{
			textpre=new LSTMRepresentation();
			break;
		}
		case "gru":
		{
			textpre=new GRURepresentation();
			break;
		}		
		}
		
		textpre.SetParameters(paraMap);
		
		
		String embfile=paraMap.get("embfile");
		Map<String,Integer> word2index=new HashMap<String,Integer>();
		double[][] wordEmbs=null;
		if(embfile!=null)
		{
			wordEmbs=textpre.InitGloveEmbs(embfile,"utf8",word2index,dim);
		}
		else
		{
			wordEmbs=textpre.InitEmbsRandomly(wordsize, dim);
		}
		textpre.SetEmbeddings(wordEmbs);
		textpre.SetWord2Index(word2index);
		textpre.SetDim(dim);

	}

	
	int zerolength=0;
	double length_rate=0;
	int sumcount=0;
	public void wzy_debug_ZeroPrint()
	{
		System.out.println("zero number is "+zerolength);
		System.out.println("zero rate is "+(double)zerolength/(double)sumcount);
		System.out.println("find rate is "+length_rate/(double)sumcount);
		
		System.out.println("unknown words is "+unknownwordSet.size());
		try {
			PrintStream ps=new PrintStream("wordSet.list");
			Iterator it=unknownwordSet.iterator();
			while(it.hasNext())
			{
				String token=(String)it.next();
				ps.println(token);
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	Set<String> unknownwordSet=new HashSet<String>();
	

	@Override
	public double ScoreQAPair(Question qus, int aindex) {
		// TODO Auto-generated method stub

		double[] qus_embs=textpre.RepresentText(qus.question_content, dim);
		double[] ans_embs=textpre.RepresentText(qus.answers[aindex], dim);
		//dot?
		//double score=ScoreQAPair(qus_embs,ans_embs);
		double score=ScoreQAPairMinusL1(qus_embs,ans_embs);

		return score;
	}
	/*public double[] getEmbs(String str)
	{
		double[][] str_embs=Text2Embs(str);
		double[] res_embs=textpre.RepresentText(str_embs, dim);
		return res_embs;
	}*/
	public double ScoreQAPair(double[] qus_embs,double[] ans_embs)
	{
		return MatrixTool.VectorDot(qus_embs, ans_embs);
	}
	public double ScoreQAPairMinusL1(double[] qus_embs,double[] ans_embs)
	{
		//return MatrixTool.VectorDot(qus_embs, ans_embs);
		double[] minus=MatrixTool.VectorMinus(qus_embs, ans_embs);
		double norm=0;
		if(normtype==1)
			norm=MatrixTool.VectorNorm1(minus);
		else
			norm=MatrixTool.VectorNorm2(minus);			
		return -norm;
	}	

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		// you can process questions to map the word to the embeddings, and find unknown words
		
	}


	@Override
	public double CalLoss(Question q) {
		// TODO Auto-generated method stub
		if(normtype==1)
			return CalLossNorm1(q);
		else
			return CalLossNorm2(q);
	}
	
	public double CalLossNorm1(Question q) {
		// TODO Auto-generated method stub
		
		//pairwise
		double[] scores=new double[q.answers.length];
		
		//question embeddings
		//double[][] qus_words_embs=Text2Embs(q.question_content);
		double[] qus_embs=textpre.RepresentText(q.question_content, dim);
		double[] neg_qus_embs=new double[qus_embs.length];

		for(int j=0;j<neg_qus_embs.length;j++)
		{
			neg_qus_embs[j]=-qus_embs[j];
		}
		
		//answer embeddings
		//double[][][] ans_words_embs=new double[q.answers.length][][];
		double[][] ans_embs=new double[q.answers.length][];
		
		
		//graidnets
		double[][] qGras=new double[q.answers.length][dim];
		double[][] aGras=new double[q.answers.length][dim];		
		
		
		//score
		for(int i=0;i<scores.length;i++)
		{
			//ans_words_embs[i]=Text2Embs(q.answers[i]);
			ans_embs[i]=textpre.RepresentText(q.answers[i], dim);
			
			double[] minus=MatrixTool.VectorMinus(qus_embs, ans_embs[i]);
			if(i==q.AnswerKey)
			{
				for(int j=0;j<dim;j++)
				{
					if(minus[j]>0)
					{
						qGras[i][j]=1;
						aGras[i][j]=-1;					
					}
					else
					{
						qGras[i][j]=-1;	
						aGras[i][j]=1;						
					}
				}
			}
			else
			{
				for(int j=0;j<dim;j++)
				{
					if(minus[j]>0)
					{
						qGras[i][j]=-1;
						aGras[i][j]=1;					
					}
					else
					{
						qGras[i][j]=1;	
						aGras[i][j]=-1;						
					}
				}				
			}
			
			//scores[i]=this.ScoreQAPairMinusL1(qus_embs,ans_embs[i]);
			scores[i]=MatrixTool.VectorNorm1(minus);
		}
		
		if(scores.length>1)
		{
			
			for(int i=0;i<scores.length;i++)
			{
				if(i==q.AnswerKey)
					continue;
				double paircost=MatrixTool.PairWiseMargin(scores[i],scores[q.AnswerKey],pairwise_margin);
				if(paircost<1e-10)
					continue;
				TrainInter trainInter=(TrainInter)textpre;
				
				
				//for right answer and q
				//trainInter.CalculateGradient(ans_words_embs[q.AnswerKey], neg_qus_embs);
				trainInter.CalculateGradient(q.answers[q.AnswerKey], aGras[q.AnswerKey]);		
				trainInter.CalculateGradient(q.question_content, qGras[q.AnswerKey]);
				//for wrong answer and q
				trainInter.CalculateGradient(q.answers[i], aGras[i]);		
				trainInter.CalculateGradient(q.question_content, qGras[i]);				
				
			}
		}
		
		
		return 0;
	}
public double CalLossNorm2(Question q) {
		// TODO Auto-generated method stub
		
		//pairwise
		double[] scores=new double[q.answers.length];
		
		//question embeddings
		//double[][] qus_words_embs=Text2Embs(q.question_content);
		double[] qus_embs=textpre.RepresentText(q.question_content, dim);
		double[] neg_qus_embs=new double[qus_embs.length];

		for(int j=0;j<neg_qus_embs.length;j++)
		{
			neg_qus_embs[j]=-qus_embs[j];
		}
		
		//answer embeddings
		//double[][][] ans_words_embs=new double[q.answers.length][][];
		double[][] ans_embs=new double[q.answers.length][];
		
		
		//graidnets
		double[][] qGras=new double[q.answers.length][dim];
		double[][] aGras=new double[q.answers.length][dim];		
		
		
		//score
		for(int i=0;i<scores.length;i++)
		{
			//ans_words_embs[i]=Text2Embs(q.answers[i]);
			ans_embs[i]=textpre.RepresentText(q.answers[i], dim);
			
			double[] minus=MatrixTool.VectorMinus(qus_embs, ans_embs[i]);
			if(i==q.AnswerKey)
			{
				for(int j=0;j<dim;j++)
				{
					qGras[i][j]=2*minus[j];
					aGras[i][j]=-2*minus[j];					
				}
			}
			else
			{
				for(int j=0;j<dim;j++)
				{
					qGras[i][j]=-2*minus[j];
					aGras[i][j]=2*minus[j];					
				}				
			}
			
			//scores[i]=this.ScoreQAPairMinusL1(qus_embs,ans_embs[i]);
			scores[i]=MatrixTool.VectorNorm2(minus);
		}
		
		if(scores.length>1)
		{
			
			for(int i=0;i<scores.length;i++)
			{
				if(i==q.AnswerKey)
					continue;
				double paircost=MatrixTool.PairWiseMargin(scores[i],scores[q.AnswerKey],pairwise_margin);
				if(paircost<1e-10)
					continue;
				TrainInter trainInter=(TrainInter)textpre;
				
				
				//for right answer and q
				//trainInter.CalculateGradient(ans_words_embs[q.AnswerKey], neg_qus_embs);
				trainInter.CalculateGradient(q.answers[q.AnswerKey], aGras[q.AnswerKey]);		
				trainInter.CalculateGradient(q.question_content, qGras[q.AnswerKey]);
				//for wrong answer and q
				trainInter.CalculateGradient(q.answers[i], aGras[i]);		
				trainInter.CalculateGradient(q.question_content, qGras[i]);				
				
			}
		}
		
		
		return 0;
	}
	@Override
	public boolean Trainable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public TrainInter GetTrainInter() {
		// TODO Auto-generated method stub
		return (TrainInter)this.textpre;
	}

	@Override
	public void InitAllGradients() {
		// TODO Auto-generated method stub
		((TrainInter)(this.textpre)).InitGradients();
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		((TrainInter)(this.textpre)).UpgradeGradients(gamma);
	}
	@Override
	public void InitPathWeightRandomly(List<Question> questionList) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	//////////////////////////////////////////////////////////
	//there are 3 methods for initializing embeddings, but they were deprecated by wzy at 12.27
		/*public void InitEmbsRandomly()
		{
			embeddings=new double[wordsize][dim];
			for(int i=0;i<wordsize;i++)
			{
				for(int j=0;j<dim;j++)
				{
					embeddings[i][j]=rand.nextDouble();
				}
			}
		}*/	
		/*public void InitEmbs(String filename) throws IOException
		{
			if(filename==null)
			{
				InitEmbsRandomly();
				return;
			}
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			int line=0;
			while((buffer=br.readLine())!=null)
			{
				String[] ss=buffer.split("\t");
				if(ss.length!=dim)
				{
					System.err.println("embedding dim is wrong.");
					System.exit(-1);
				}
				for(int i=0;i<dim;i++)
				{
					embeddings[line][i]=Double.parseDouble(ss[i]);
				}
				line++;
			}
			if(line!=wordsize)
			{
				System.err.println("there is a different number of words in embedding file.");
				System.exit(-1);
			}
		}*/
		/*public void InitGloveEmbs(String filename,String code) throws IOException
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
			String buffer=null;
			word2index=new HashMap<String,Integer>();
			List<double[]> embeddingList=new ArrayList<double[]>();
			while((buffer=br.readLine())!=null)
			{
				String[] ss=buffer.split(" ");
				String token=ss[0];
				
				Integer index=word2index.get(token);
				if(index!=null)
					continue;
				
				double[] emb=new double[dim];
				for(int i=0;i<dim;i++)
				{
					emb[i]=Double.parseDouble(ss[i+1]);
				}

				embeddingList.add(emb);
				word2index.put(token,word2index.size());
			}
			br.close();
			
			embeddings=embeddingList.toArray(new double[0][]);
			
			if(word2index.size()!=embeddings.length)
			{
				System.err.println("there is a different number of words in embedding file.");
				System.exit(-1);
			}
			
		}*/
}
