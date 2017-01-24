package org.wzy.fun.merge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.wzy.meta.*;
import org.wzy.tool.IOTool;

public class Combining {

	

	public Map<String,double[]> lucene_scores;
	public Map<String,double[]> ti_scores;	
	public Map<String,double[]> inf_scores;		
	
	public Map<String,double[]> ReadScores(String filename) throws IOException
	{
		Map<String,double[]> qid2scores=new HashMap<String,double[]>();
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=5)
				continue;
			double[] scores=new double[4];
			for(int i=0;i<4;i++)
			{
				scores[i]=Double.parseDouble(ss[i+1]);
			}
			qid2scores.put(ss[0], scores);
		}
		br.close();
		return qid2scores;
	}
	
	public int[] voting(List<Question> qList)
	{
		int[] res=new int[qList.size()];
		
		Map<String,double[]>[] maps=new Map[3];
		maps[0]=this.lucene_scores;
		maps[1]=this.ti_scores;
		maps[2]=this.inf_scores;
		
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			int[] tmp_res=new int[maps.length];
			for(int j=0;j<maps.length;j++)
			{
				double[] scores=maps[j].get(q.questionID);
				int maxindex=0;
				for(int k=1;k<scores.length;k++)
				{
					if(scores[maxindex]<scores[k])
					{
						maxindex=k;
					}
				}
				tmp_res[j]=maxindex;
			}
			
			/*int[] votes=new int[q.answers.length];
			for(int j=0;j<tmp_res.length;j++)
			{
				votes[tmp_res[j]]++;
			}*/
			
			if(tmp_res[1]==tmp_res[2])
			{
				res[i]=tmp_res[1];
			}
			else
				res[i]=tmp_res[0];	
		}
		return res;
	}
	
	public int[] suming(List<Question> qList)
	{
		int[] res=new int[qList.size()];
		Map<String,double[]>[] maps=new Map[3];
		maps[0]=this.lucene_scores;
		maps[1]=this.ti_scores;
		maps[2]=this.inf_scores;
		
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			double[] sums=new double[q.answers.length];
			
			for(int j=0;j<maps.length;j++)
			{
				double[] scores=maps[j].get(q.questionID);
				for(int k=0;k<scores.length;k++)
				{
					sums[k]+=scores[k];
				}
			}
			
			int maxindex=0;
			for(int k=1;k<sums.length;k++)
			{
				if(sums[maxindex]<sums[k])
				{
					maxindex=k;
				}
			}
			res[i]=maxindex;
		}
		
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
	
	double[] weights;
	public int[] learning(List<Question> qList)
	{
		int[] res=new int[qList.size()];
		Map<String,double[]>[] maps=new Map[3];
		maps[0]=this.lucene_scores;
		maps[1]=this.ti_scores;
		//maps[1]=this.lucene_scores;
		//maps[1]=new HashMap<String,double[]>();
		maps[2]=this.inf_scores;
		maps[2]=new HashMap<String,double[]>();		
		
		weights=new double[3];
		weights[0]=1;
		weights[1]=0;
		weights[2]=0;		
		
		double gamma=1e-6;
		
		//training
		for(int epoch=0;epoch<4000;epoch++)
		{
			double[] gradients=new double[3];
			for(int i=0;i<qList.size();i++)
			{
				Question q=qList.get(i);
				double[] sums=new double[q.answers.length];
				double[][] scores=new double[maps.length][];
				for(int j=0;j<maps.length;j++)
				{
					scores[j]=maps[j].get(q.questionID);
					if(scores[j]==null)
					{
						scores[j]=new double[4];
						for(int k=0;k<scores.length;k++)
							scores[j][k]=0.;
					}
					for(int k=0;k<scores.length;k++)
					{
						sums[k]+=scores[j][k]*weights[j];
					}
				}
			
				int maxindex=0;
				for(int k=1;k<sums.length;k++)
				{
					if(sums[maxindex]<sums[k])
					{
						maxindex=k;
					}
				}
				if(maxindex==q.AnswerKey)
					continue;
				
				for(int j=0;j<3;j++)
				{
					gradients[j]+=scores[j][maxindex]-scores[j][q.AnswerKey];
				}
			}	
			
			for(int i=0;i<3;i++)
			{
				weights[i]-=gamma*gradients[i];
			}
			

			double sum=0;
			for(int i=0;i<3;i++)
			{
				sum+=weights[i];
			}
			for(int i=0;i<3;i++)
			{
				weights[i]/=sum;
			}	
			
			System.out.println(weights[0]+"\t"+weights[1]+"\t"+weights[2]);
			
		}
		
		
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			double[] sums=new double[q.answers.length];
			
			for(int j=0;j<maps.length;j++)
			{
				double[] scores=maps[j].get(q.questionID);
				if(scores==null)
				{
					scores=new double[4];
					for(int k=0;k<scores.length;k++)
						scores[k]=0.;
				}
				for(int k=0;k<scores.length;k++)
				{
					sums[k]+=scores[k]*weights[j];
				}
			}
			
			int maxindex=0;
			for(int k=1;k<sums.length;k++)
			{
				if(sums[maxindex]<sums[k])
				{
					maxindex=k;
				}
			}
			res[i]=maxindex;
		}
		
		
		return res;
	}
	
	
	public static void main(String[] args) throws IOException
	{
		Combining comb=new Combining();
		
		//comb.lucene_scores=comb.ReadScores("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\path_tmp_best_scores_1.16v\\middle_lucene.normscore");
		//comb.ti_scores=comb.ReadScores("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\path_tmp_best_scores_1.16v\\middle_test_1.16v_ti.score.norm");
		//comb.inf_scores=comb.ReadScores("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\path_tmp_best_scores_1.16v\\middle_test_1.16v.score.norm");
		//List<Question> qList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-MiddleSchool-NDMC-Test.csv.simple", "utf8");
		
		
		comb.lucene_scores=comb.ReadScores("D:\\KBQA\\DataSet\\ck12_6000\\5dataset_analysis\\gru_results\\cbiology.ti.score");
		comb.ti_scores=comb.ReadScores("D:\\KBQA\\DataSet\\ck12_6000\\5dataset_analysis\\bestresults\\cbiology.path.score");
		List<Question> qList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolBiology\\cbiology_inf_test.segment.300.concept.exactly", "utf8");
		
		
		//int[] results=comb.voting(qList);
		//int[] results=comb.suming(qList);	
		int[] results=comb.learning(qList);	
		int[] ans=comb.GetAnswers(qList);
		double score=comb.Evluation(results, ans);
		
		
		System.out.println("Correct rate is "+score);
		
		
	}
	
}
