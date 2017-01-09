package org.wzy.kb.fb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.wzy.method.scImpl.WordEmbScorer;
import org.wzy.method.trImpl.SumRepresentation;
import org.wzy.tool.IOTool;

public class ProduceEmbeddingsForInitKB {

	public int max_entity_id=-1;
	public int max_relation_id=-1;
	
	public Map<String,String> mid2name;
	public SumRepresentation embeddingTool;
	
	public List<String> entityList;
	public List<String> relationList;
	
	public double[][] entityEmbedding;
	public double[][] relationEmbedding;
	public int dim=50;
	public Random rand=new Random();
	
	public int entity_rand_count=0;
	public int relation_rand_count=0;
	
	public void ReadAllFacts(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			for(int i=0;i<3;i+=2)
			{
				int index=Integer.parseInt(ss[i]);
				if(index>max_entity_id)
				{
					max_entity_id=index;
				}
			}
			int index=Integer.parseInt(ss[1]);
			if(index>max_relation_id)
			{
				max_relation_id=index;
			}
		}
		
		br.close();
		
	}
	
	public void InitRandomly(double[] emb,int dim)
	{
		for(int i=0;i<dim;i++)
		{
			emb[i]=rand.nextDouble();
		}
	}
	
	public void InitByWordse(String str,double[] emb,int dim)
	{
		String[] ss=str.split("_");
		int count=0;
		for(int i=0;i<ss.length;i++)
		{
			Integer index=embeddingTool.word2index.get(ss[i]);
			if(index!=null)
			{
				count++;
				for(int j=0;j<dim;j++)
				{
					//System.out.println(emb.length+"\t"+embeddingTool.embeddings.length+"\t"+embeddingTool.embeddings[index].length);
					emb[j]+=embeddingTool.embeddings[index][j];
				}
			}
		}
		if(count<=0)
		{	
			entity_rand_count++;
			InitRandomly(emb,dim);
		}
	}
	public void InitByWordsr(String str,double[] emb,int dim)
	{
		String[] ss=str.split("_");
		int count=0;
		for(int i=0;i<ss.length;i++)
		{
			Integer index=embeddingTool.word2index.get(ss[i]);
			if(index!=null)
			{
				count++;
				for(int j=0;j<dim;j++)
				{
					//System.out.println(emb.length+"\t"+embeddingTool.embeddings.length+"\t"+embeddingTool.embeddings[index].length);
					emb[j]+=embeddingTool.embeddings[index][j];
				}
			}
		}
		if(count<=0)
		{	
			relation_rand_count++;
			InitRandomly(emb,dim);
		}
	}	
	
	public void ProduceEntityEmbeddings()
	{
		entityEmbedding=new double[entityList.size()][dim];
		for(int i=0;i<entityList.size();i++)
		{
			String mid=entityList.get(i);
			String name=mid2name.get(mid);
			if(name==null)
			{
				entity_rand_count++;
				InitRandomly(entityEmbedding[i],dim);	
			}
			else
			{
				InitByWordse(name,entityEmbedding[i],dim);		
			}
		}
	}
	
	public void ProduceRelationEmbeddings()
	{
		relationEmbedding=new double[relationList.size()][dim];
		for(int i=0;i<relationList.size();i++)
		{
			String rel=relationList.get(i);		
			String[] ss=rel.split("\\.");
			System.out.println(rel+"\t"+ss.length);
			if(ss.length<=0)
				continue;
			relation_rand_count++;
			rel=ss[ss.length-1];
			InitByWordsr(rel,relationEmbedding[i],dim);
		}
	}

	
	
	public static void main(String[] args) throws IOException
	{
		ProduceEmbeddingsForInitKB pe=new ProduceEmbeddingsForInitKB();
		
		long start=System.currentTimeMillis();
		
		//read word embedding
		pe.embeddingTool=new SumRepresentation();
		pe.embeddingTool.dim=pe.dim;
		double[][] wordEmbs=null;
		Map<String,Integer> word2index=new HashMap<String,Integer>();

		wordEmbs=pe.embeddingTool.InitGloveEmbs(args[0],"utf8",word2index,pe.dim);
		pe.embeddingTool.SetEmbeddings(wordEmbs);
		pe.embeddingTool.SetWord2Index(word2index);
		

			
		//get entity id to name by read all entity names in freebase
		/*pe.entityList=IOTool.ReadLargeStringList(args[1], "utf8", 2000000);
		pe.mid2name=IOTool.ReadEntityMidtoName(args[2], "utf8");	
		pe.ProduceEntityEmbeddings();
		IOTool.PrintDoubleMatrix(pe.entityEmbedding, args[4], 6);
		pe.mid2name=null;
		pe.entityEmbedding=null;
		pe.entityList=null;
		long end=System.currentTimeMillis();
		System.out.println("init entity embedding takes "+(end-start)+" ms, entity rand "+pe.entity_rand_count);*/
		
		
		//get relation id to name
		pe.relationList=IOTool.ReadLargeStringList(args[1], "utf8", 0, 10000);
		pe.ProduceRelationEmbeddings();
		IOTool.PrintDoubleMatrix(pe.relationEmbedding, args[2], 6);
		start=System.currentTimeMillis();
		//System.out.println("init relation embedding takes "+(start-end)+" ms, relation rand "+pe.relation_rand_count);
		System.out.println(pe.relation_rand_count);
		
	}
}
