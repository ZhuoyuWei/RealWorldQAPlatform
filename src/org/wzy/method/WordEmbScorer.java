package org.wzy.method;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.wzy.meta.NNParameter;
import org.wzy.meta.Question;
import org.wzy.tool.MatrixTool;

public class WordEmbScorer implements ScoringInter{

	public Random rand=new Random();
	public Map<String,Integer> word2index;
	public double[][] embeddings;
	
	public int wordsize;
	public int dim;
	public TextRepresentInter textpre;
	public double pairwise_margin;
	
	public void InitEmbsRandomly()
	{
		embeddings=new double[wordsize][dim];
		for(int i=0;i<wordsize;i++)
		{
			for(int j=0;j<dim;j++)
			{
				embeddings[i][j]=rand.nextDouble();
			}
		}
	}
	
	public void InitEmbs(String filename) throws IOException
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
	}

	

	@Override
	public void InitScorer(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		
		String embfile=paraMap.get("embfile");
		try {
			InitEmbs(embfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		}
		
		textpre.SetParameters(paraMap);
	}

	public double[][] Text2Embs(String str)
	{
		List<double[]> embList=new ArrayList<double[]>();
		String[] ss=str.split("\t");
		for(int i=0;i<ss.length;i++)
		{
			Integer index=word2index.get(ss[i]);
			if(index!=null)
			{
				embList.add(embeddings[index]);
			}
		}
		return embList.toArray(new double[0][]);
	}
	
	
	

	@Override
	public double ScoreQAPair(Question qus, int aindex) {
		// TODO Auto-generated method stub

		double[] qus_embs=getEmbs(qus.question_content);
		double[] ans_embs=getEmbs(qus.answers[aindex]);
		//dot?
		double score=ScoreQAPair(qus_embs,ans_embs);

		return score;
	}
	public double[] getEmbs(String str)
	{
		double[][] str_embs=Text2Embs(str);
		double[] res_embs=textpre.RepresentText(str_embs, dim);
		return res_embs;
	}
	public double ScoreQAPair(double[] qus_embs,double[] ans_embs)
	{
		return MatrixTool.VectorDot(qus_embs, ans_embs);
	}
	

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		// you can process questions to map the word to the embeddings, and find unknown words
		
	}


	@Override
	public double CalLoss(Question q) {
		// TODO Auto-generated method stub
		
		//pairwise
		double[] scores=new double[q.answers.length];
		
		//question embeddings
		double[][] qus_words_embs=Text2Embs(q.question_content);
		double[] qus_embs=textpre.RepresentText(qus_words_embs, dim);
		double[] neg_qus_embs=new double[qus_embs.length];
		for(int j=0;j<neg_qus_embs.length;j++)
		{
			neg_qus_embs[j]=-qus_embs[j];
		}
		
		//answer embeddings
		double[][][] ans_words_embs=new double[q.answers.length][][];
		double[][] ans_embs=new double[q.answers.length][];
		
		//score
		for(int i=0;i<scores.length;i++)
		{
			ans_words_embs[i]=Text2Embs(q.answers[i]);
			ans_embs[i]=textpre.RepresentText(ans_words_embs[i], dim);
			scores[i]=ScoreQAPair(qus_embs,ans_embs[i]);
		}
		
		if(scores.length>1)
		{
			for(int i=0;i<scores.length;i++)
			{
				if(i==q.AnswerKey)
					continue;
				double paircost=PairWiseMargin(scores[q.AnswerKey],scores[i],pairwise_margin);
				if(paircost<1e-10)
					continue;
				TrainInter trainInter=(TrainInter)textpre;
				
				
				
				//for right answer
				trainInter.CalculateGradient(ans_words_embs[q.AnswerKey], neg_qus_embs);
				
				//for wrong answer
				trainInter.CalculateGradient(ans_words_embs[i], qus_embs);
				
				//for question
				double[] ans_minus_embs=new double[qus_embs.length];
				for(int j=0;j<qus_embs.length;j++)
				{
					ans_minus_embs[j]=ans_embs[i][j]-ans_embs[q.AnswerKey][j];
				}
				trainInter.CalculateGradient(qus_words_embs, ans_minus_embs);
				
			}
		}
		
		
		return 0;
	}
	
	public double PairWiseMargin(double right,double wrong,double margin)
	{
		return Math.max(wrong+margin-right, 0.);
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
	
}
