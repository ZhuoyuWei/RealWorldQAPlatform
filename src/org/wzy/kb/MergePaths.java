package org.wzy.kb;

import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.ConceptPath;
import org.wzy.meta.Question;
import org.wzy.method.KBModel;
import org.wzy.tool.IOTool;

public class MergePaths {

	public static int maxRelation(List<Question> qList)
	{
		int maxrel=-1;
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			for(int j=0;j<q.ans_paths.length;j++)
			{
				for(int k=0;k<q.ans_paths[j].length;k++)
				{
					ConceptPath cp=q.ans_paths[j][k];
					for(int h=0;h<cp.relationList.size();h++)
					{
						int rel=cp.relationList.get(h)<KBModel.reverseEdge?cp.relationList.get(h):cp.relationList.get(h)-KBModel.reverseEdge;
						if(maxrel<rel)
							maxrel=rel;
					}
				}
			}
		}
		return maxrel;
	}
	
	public static void TransformCPs(List<Question> qList,int relsize)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			for(int j=0;j<q.ans_paths.length;j++)
			{
				for(int k=0;k<q.ans_paths[j].length;k++)
				{
					ConceptPath cp=q.ans_paths[j][k];
					List<Integer> tmpList=new ArrayList<Integer>();
					for(int h=0;h<cp.relationList.size();h++)
					{
						tmpList.add(cp.relationList.get(h)+relsize);
					}
					cp.relationList=tmpList;
				}
			}
		}		
	}
	
	public static double CountPaths(List<Question> qList)
	{
		int qcount=0;
		
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			int pathcount=0;
			for(int j=0;j<q.ans_paths.length;j++)
			{
				for(int k=0;k<q.ans_paths[j].length;k++)
				{
					pathcount++;
				}
			}
			if(pathcount>0)
				qcount++;
		}			
		
		return qcount/(double)qList.size();
	}
	
	public static void main(String[] args)
	{
		List<Question> fbList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\"
				+ "AI2LicensedScienceQuestions_NoDiagrams_All\\fb_pathsDataset\\for_statistics\\Exam01-EM-NDMC.csv.simple.stem.withpath.exactly", "utf8");
		List<Question> cnList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\"
				+ "AI2LicensedScienceQuestions_NoDiagrams_All\\fb_pathsDataset\\for_statistics\\Exam01-EM-NDMC.csv.simple.stem.concept.exactly", "utf8");		
		
		int fb_relsize=maxRelation(fbList)+1;
		int cn_relsize=maxRelation(cnList)+1;
		
		double fb_qrate=CountPaths(fbList);
		double cn_qrate=CountPaths(cnList);
		
		System.out.println("fb: "+fb_relsize+"\t"+fb_qrate);
		System.out.println("cn: "+cn_relsize+"\t"+cn_qrate);		
		
		//TransformCPs(fbList,cn_relsize);
		
		
		
		
		
		
	}
	
}
