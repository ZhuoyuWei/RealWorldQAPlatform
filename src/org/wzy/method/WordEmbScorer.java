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

public class WordEmbScorer implements ScoringInter{

	public Random rand=new Random();
	public Map<String,Integer> word2index;
	public double[][] embeddings;
	
	public int wordsize;
	public int dim;
	public TextRepresentInter textpre;
	
	
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
		}
		
		textpre.SetParameters(nnpara);
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
		
		double[][] qus_word_embs=Text2Embs(qus.question_content);
		double[] qus_embs=textpre.RepresentText(qus_word_embs, dim);
		
		double[][] ans_word_embs=Text2Embs(qus.answers[aindex]);
		double[] ans_embs=textpre.RepresentText(ans_word_embs, dim);
		
		
		
		return 0;
	}

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		// you can process questions to map the word to the embeddings, and find unknown words
		
	}
	
}
