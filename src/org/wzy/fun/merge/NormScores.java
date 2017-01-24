package org.wzy.fun.merge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.meta.*;
import org.wzy.tool.IOTool;

public class NormScores {

	public Map<String,double[]> qid2scores=new HashMap<String,double[]>();
	
	public void ReadScores(String filename) throws IOException
	{
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
	}
	
	public void AttachScore2Question(List<Question> qList)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			q.scores=qid2scores.get(q.questionID);
			if(q.scores==null)
			{
				System.err.println(q.questionID+" has no scores error");
			}
			else
				Norming(q.scores);
		}
	}
	
	public void Norming(double[] scores)
	{
		double max=0.;
		double min=Double.MAX_VALUE;
		for(int i=0;i<scores.length;i++)
		{
			if(max<scores[i])
			{
				max=scores[i];
			}
			if(min>scores[i])
			{
				min=scores[i];
			}
		}
		
		double tmp=max-min;
		
		
		
		for(int i=0;i<scores.length;i++)
		{
			scores[i]-=min;
			if(tmp>1e-2)
				scores[i]/=tmp;
		}
		
		
	}
	
	public static void main(String[] args) throws IOException
	{
		NormScores ns=new NormScores();
		List<Question> qList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-Elementary-NDMC-Test.csv.simple", "utf8");
		System.out.println("q size "+qList.size());
		ns.ReadScores("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\path_tmp_best_scores_1.16v\\elementary_test_1.16v_1000.score");
		ns.AttachScore2Question(qList);
		IOTool.PrintQuestionsPredictScores(qList, "D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\path_tmp_best_scores_1.16v\\elementary_test_1.16v_1000.score.norm", "utf8");
	}
	
}
