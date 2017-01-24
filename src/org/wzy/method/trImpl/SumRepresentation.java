package org.wzy.method.trImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wzy.meta.NNParameter;
import org.wzy.method.TextRepresentInter;
import org.wzy.method.TrainInter;
import org.wzy.tool.MatrixTool;

public class SumRepresentation implements TextRepresentInter,TrainInter{


    public List<double[]> gwords;
    public List<double[][]> swords;
    //public List<Integer> word_gradient_list;
    
	public Map<String,Integer> word2index;
	public double[][] embeddings;  
	public Set<String> unknownwordSet=new HashSet<String>();
	
	public double[][] gradients;
	
	public int dim;
	
	public boolean hasL2=true;
	public double C_L2=0.;
	
	public boolean normL1=false;
	
	@Override
	public double[] RepresentText(double[][] textEmbs,int dim) {
		// TODO Auto-generated method stub
		double[] res=new double[dim];
		for(int i=0;i<textEmbs.length;i++)
		{
			for(int j=0;j<dim;j++)
			{
				res[j]+=textEmbs[i][j];
			}
		}
		
		if(textEmbs.length>1)
		{
			for(int i=0;i<dim;i++)
			{
				res[i]/=textEmbs.length;
			}
		}
		
		return res;
	}
	@Override
	public double[] RepresentText(String str, int dim) {
		// TODO Auto-generated method stub
		double[][] token_embs=Text2Embs(str);
		return RepresentText(token_embs,dim);
	}

	@Override
	public void SetParameters(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void CalculateGradient(double[][] words_embs, double[] loss) {
		// TODO Auto-generated method stub
		gwords.add(loss);
		swords.add(words_embs);
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		
		Set<double[]> wordSet=new HashSet<double[]>();
		
		for(int i=0;i<gwords.size();i++)
		{
			double[][] wordembs=swords.get(i);
			double[] gradients=gwords.get(i);
			for(int j=0;j<wordembs.length;j++)
			{
				for(int k=0;k<wordembs[j].length;k++)
				{
					wordembs[j][k]-=gamma*(gradients[k]+wordembs[j][k]*C_L2*2);
				}
				if(normL1)
				{
					wordSet.add(wordembs[j]);
				}
			}
		}
		
		if(normL1)
		{
			Iterator it=wordSet.iterator();
			while(it.hasNext())
			{
				double[] emb=(double[])it.next();
				double norm1=MatrixTool.VectorNorm1(emb);
				if(norm1>1)
				{
					norm1=1./norm1;
					for(int i=0;i<emb.length;i++)
					{
						emb[i]*=norm1;
					}
				}
			}
		}
		
		/*for(int i=0;i<gradients.length;i++)
		{
			if(gradients[i].length<=0||Math.abs(gradients[i][0])<1e-10)
				continue;
			for(int j=0;j<dim;j++)
			{
				embeddings[i][j]-=gamma*(gradients[i][j]+2*embeddings[i][j]*C_L2);
			}
		}*/
		
		/*if(hasL2)
		{
			double tmpnorm=gamma*2*C_L2;
			for(int i=0;i<gradients.length;i++)
			{
				if(gradients[i].length<=0||Math.abs(gradients[i][0])<1e-10)
					continue;
				for(int j=0;j<dim;j++)
				{
					embeddings[i][j]-=tmpnorm*embeddings[i][j];
				}
			}
		}*/
		
	}

	@Override
	public void InitGradients() {
		// TODO Auto-generated method stub
		gwords=new ArrayList<double[]>();
		swords=new ArrayList<double[][]>();
		
		//gradients=new double[embeddings.length][dim];
		
	}

	@Override
	public void SetEmbeddings(double[][] embeddings) {
		// TODO Auto-generated method stub
		this.embeddings=embeddings;
	}
	@Override
	public void SetWord2Index(Map<String, Integer> word2index) {
		// TODO Auto-generated method stub
		this.word2index=word2index;
	}
	@Override
	public double[][] Text2Embs(String str)
	{
		List<double[]> embList=new ArrayList<double[]>();
		String[] ss=str.split("[\\s]+");
		int hasword=0;
		for(int i=0;i<ss.length;i++)
		{
			Integer index=word2index.get(ss[i].trim().toLowerCase());
			if(index!=null)
			{
				embList.add(embeddings[index]);
				hasword++;
			}
			else
			{
				unknownwordSet.add(ss[i].trim().toLowerCase());
			}
		}
		return embList.toArray(new double[0][]);
	}
	@Override
	public List<Integer> Text2Index(String str) {
		// TODO Auto-generated method stub
		List<Integer> indexList=new ArrayList<Integer>();
		String[] ss=str.split("[\\s]+");
		for(int i=0;i<ss.length;i++)
		{
			Integer index=word2index.get(ss[i].trim().toLowerCase());
			if(index!=null)
			{
				indexList.add(index);
				
			}
			
		}
		return indexList;	
	}
	@Override
	public void CalculateGradient(String text, double[] loss) {
		// TODO Auto-generated method stub
/*		List<Integer> indexList=Text2Index(text);
		for(int i=0;i<indexList.size();i++)
		{
			int index=indexList.get(i);
			for(int j=0;j<loss.length;j++)
			{
				gradients[index][j]+=loss[j];
			}
		}*/
		
		double[][] wordembs=Text2Embs(text);
		CalculateGradient(wordembs,loss);
		
	}
	@Override
	public void SetDim(int dim) {
		// TODO Auto-generated method stub
		this.dim=dim;
	}

}
