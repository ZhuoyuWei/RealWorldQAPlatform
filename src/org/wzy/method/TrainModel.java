package org.wzy.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.wzy.meta.Question;
import org.wzy.tool.MatrixTool;

public class TrainModel {

	//public double[][] entity_embedding
	
	public boolean L1regular=false;
	public boolean project=false;
	public boolean trainprintable=false;//true;	
	public Random rand=new Random();	
	
	public int Epoch=100;
	public int minibranchsize=64;
	public double gamma=0.01;
	public double margin=1.;
	public int random_data_each_epoch=10000;
	public boolean bern=false;//true;//false;//true;

	
	
	public double lammadaL1=0.;
	public double lammadaL2=0;
	

	
	public String printMiddleModel_dir=null;
	public int printEpoch=100;
	
	//for debug
	public int errcount=0;
	public boolean quiet=false;
	public String print_log_file=null;
	
	
	public ScoringInter scoreInter;
	public TrainInter trainInter;
	
	public void Training(Question[] traindatas)
	{
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
				int a=Math.abs(rand.nextInt())%traindatas.length;
				int b=Math.abs(rand.nextInt())%traindatas.length;	
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
				trainInter.InitGradients();
				OneBranchTraining(traindatas,sindex,eindex);	
			}
			long end=System.currentTimeMillis();

			System.err.println("Epoch "+epoch+" is end at "+(end-start)/1000+"s");


			
		}
	}
	
	/**
	 * Need to be overwrote
	 * init all embeddings
	 * because different model has its different structure of entity and relation 
	 * and different methods of initializing
	 * @need Override
	 */
	public void InitEmbeddingsRandomly(int[][] triplets)
	{}
	
	public void InitEmbeddingsMemory()
	{}
	
	public void InitEmbeddingFromFile(String filename)
	{}
	public void InitPathFromFile(String filename)
	{}
	
	
	/**
	 * Need to be overwrote
	 * calculate similarity score for a triplet
	 * because different model has its different structure of entity and relation 
	 * and have different usage of entity or relation embeddings.
	 * Also, it can be L1-norm, L2-norm, or product between several vectors.
	 * @param triplet
	 * @return the similarity score of a triplet
	 * @need Override
	 */
	public double CalculateSimilarity(int[] triplet)
	{
		return 0;
	}
	
	/**
	 * Need to be overwrote
	 * Calculate gradients for both entity and relation embeddings, and may include other weights matrix and so on.
	 * However, different model has different method of calculating gradients.
	 * This function need be overwrote.
	 * @param triplet calculate gradient for it
	 * and they need cast the objects to embeddings in this method by themselves.
	 * @need Override
	 */

	
	/**
	 * Upgrade all parameters in your model.
	 * @param embeddingList 
	 * @param gradientList
	 */
	public void UpgradeGradients(List<Object> embeddingList,List<Object> gradientList)
	{
		for(int i=0;i<embeddingList.size();i++)
		{
			if(embeddingList.get(i) instanceof double[][][])
			{
				UpgradeGradients((double[][][])embeddingList.get(i),(double[][][])gradientList.get(i));
			}
			else if(embeddingList.get(i) instanceof double[][])
			{
				UpgradeGradients((double[][])embeddingList.get(i),(double[][])gradientList.get(i));	
			}
			else if(embeddingList.get(i) instanceof double[])
			{
				UpgradeGradients((double[])embeddingList.get(i),(double[])gradientList.get(i));			
			}
			else
			{
				UpgradeGradients(embeddingList,gradientList,i);
			}
			//System.exit(-1);
		}
	}
	public void UpgradeGradients(double[] embedding,double[] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			if(Math.abs(gradient[i])<1e-6)
				continue;
			//double tmp=embedding[i];
			embedding[i]+=gamma*gradient[i]*(-1);
			//System.out.println("my see: "+tmp+" "+embedding[i]);
		}
	}
	public void UpgradeGradients(double[][] embedding,double[][] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			//System.out.println("i: "+i);
			UpgradeGradients(embedding[i],gradient[i]);
		}
	}	
	public void UpgradeGradients(double[][][] embedding,double[][][] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			UpgradeGradients(embedding[i],gradient[i]);
		}
	}	
	/**
	 * Need to be overwrote. Other type parameters for your model.
	 * For example, a double value.
	 * @param embeddingList
	 * @param gradientList
	 * @param index
	 * @need Override
	 */
	public void UpgradeGradients(List<Object> embeddingList,List<Object> gradientList,int index)
	{}
	

	/**
	 * After changing any parameter vector when training,it need 
	 * @param embeddingList
	 */
	public void BallProjecting(List<Object> embeddingList)
	{
		for(int i=0;i<embeddingList.size();i++)
		{
			if(embeddingList.get(i) instanceof double[][][])
			{
				if(L1regular)
					L1BallProjecting((double[][][])embeddingList.get(i));
				else
					L2BallProjecting((double[][][])embeddingList.get(i));	
			}			
			else if(embeddingList.get(i) instanceof double[][])
			{
				if(L1regular)
					L1BallProjecting((double[][])embeddingList.get(i));
				else
					L2BallProjecting((double[][])embeddingList.get(i));	
			}
			else if(embeddingList.get(i) instanceof double[])
			{
				if(L1regular)
					L1BallProjecting((double[])embeddingList.get(i));
				else
					L2BallProjecting((double[])embeddingList.get(i));				
			}

		}
	}
	public void L1BallProjecting(double[][][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			double x=MatrixTool.MatrixNorm1(embeddings[i]);
			if(x>1)
			{
				for(int j=0;j<embeddings[i].length;j++)
				{
					for(int k=0;k<embeddings[i][j].length;k++)
					{
						embeddings[i][j][k]/=x;
					}
				}
			}
		}
	}
	public void L2BallProjecting(double[][][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			double x=MatrixTool.MatrixNorm2(embeddings[i]);
			if(x>1)
			{
				for(int j=0;j<embeddings[i].length;j++)
				{
					for(int k=0;k<embeddings[i][j].length;k++)
					{
						embeddings[i][j][k]/=x;
					}
				}
			}
		}
	}	
	public void L1BallProjecting(double[][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			L1BallProjecting(embeddings[i]);
		}
	}
	public void L2BallProjecting(double[][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			L2BallProjecting(embeddings[i]);
		}
	}	
	public void L1BallProjecting(double[] embeddings)
	{
		double x=0.;
		for(int i=0;i<embeddings.length;i++)
		{
			x+=Math.abs(embeddings[i]);
		}
		if(x>1.)
		{
			x=1./x;
			for(int i=0;i<embeddings.length;i++)
			{
				embeddings[i]*=x;
			}
		}
	}
	public void L2BallProjecting(double[] embeddings)
	{
		double x=0.;
		for(int i=0;i<embeddings.length;i++)
		{
			x+=embeddings[i]*embeddings[i];
		}
		if(x>1.)
		{
			x=1./Math.sqrt(x);
			for(int i=0;i<embeddings.length;i++)
			{
				embeddings[i]*=x;
			}
		}
	}	
	
	


	/**
	 * In training process, this function is a unit of learning embeddings at one branch.
	 * It includes : (1) calculate gradients for each triplet in this branch; 
	 *               (2) upgrade embeddings by using gradients calculated;
	 *               (3) project each embedding vector to a L1 or L2 ball. 
	 * @param traindatas
	 * @param sindex
	 * @param eindex
	 */
	public void OneBranchTraining(Question[] traindatas,int sindex,int eindex)
	{
		//List<Object> embeddingList=trainInter.ListingEmbedding();
		//List<Object> gradientList=trainInter.ListingGradient();
		for(int i=sindex;i<=eindex;i++)
		{
			//trainInter.CalculateGradient(traindatas[i]);
			scoreInter.CalLoss(traindatas[i]);
		}
		
		//UpgradeGradients(embeddingList,gradientList);
		trainInter.UpgradeGradients(gamma);
		/*if(project)
			BallProjecting(embeddingList);
		else
			RegularEmbedding(traindatas,sindex,eindex);*/
	}
	
	/**
	 * Need to be overwrote.
	 * Regular embeddings, if project flag is set as false, it doesn't use standard projecting algorithm,
	 * but uses model's own regular function.
	 * @need override
	 * @param traindatas
	 * @param sindex
	 * @param eindex
	 */
	public void RegularEmbedding(Question[] traindatas,int sindex,int eindex)
	{}
	
	public boolean DiscriminateTripletMinium(double score)
	{
		return score<margin;
	}
	public boolean DiscriminateTripletMinium(double score,double falsescore)
	{
		return score+margin<falsescore;
	}	
	public boolean DiscriminateTripletMaxium(double score)
	{
		return score>margin;
	}
	public boolean DiscriminateTripletMaxium(double score,double falsescore)
	{
		return score-margin>falsescore;
	}		
	
	/**
	 * It is used to calculate errors for training, and to judge whether the learning process can stop.
	 * This function return the least similarity score for triplet.
	 * @param triplet
	 * @return 
	 */
	public double CalculatePointError(int[] triplet)
	{
		return CalculateSimilarity(triplet);
		//return DiscriminateTripletMinium(similarity)?(similarity-1):0;	
	}



}