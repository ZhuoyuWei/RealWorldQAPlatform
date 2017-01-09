package org.wzy.method;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.wzy.meta.NNParameter;

public interface TextRepresentInter {

	public double[] RepresentText(double[][] textEmbs,int dim);
	public double[] RepresentText(String str,int dim);	
	//public void SetParameters(NNParameter para);
	
	//public void SetWordEmbs(double[][] embs);
	//public void InitGradients();
	
	public void SetParameters(Map<String,String> paraMap);
	public void SetEmbeddings(double[][] embeddings);
	public void SetWord2Index(Map<String,Integer> word2index);
	public void SetDim(int dim);
	
	
	public double[][] Text2Embs(String str);
	public List<Integer> Text2Index(String str);
	
	///////////////////////default methods//////////////////////////
	
	default public double[][] InitEmbsRandomly(int wordsize,int dim)
	{
		double[][] embeddings=new double[wordsize][dim];
		Random rand=new Random();
		for(int i=0;i<wordsize;i++)
		{
			for(int j=0;j<dim;j++)
			{
				embeddings[i][j]=rand.nextDouble();
			}
		}
		return embeddings;
	}
	
	default public double[][] InitEmbs(String filename,int wordsize,int dim) throws IOException
	{
		double[][] embeddings=null;
		if(filename==null)
		{
			embeddings=InitEmbsRandomly(wordsize,dim);
			return embeddings;
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
		return embeddings;
	}
	
	default public double[][] InitGloveEmbs(String filename,String code,Map<String,Integer> word2index,int dim) 
	{
		BufferedReader br=null;
		List<double[]> embeddingList=new ArrayList<double[]>();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
			String buffer=null;
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
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double[][] embeddings=embeddingList.toArray(new double[0][]);
		
		if(word2index.size()!=embeddings.length)
		{
			System.err.println("there is a different number of words in embedding file.");
			System.exit(-1);
		}
		return embeddings;
	}
	

	
	
}
