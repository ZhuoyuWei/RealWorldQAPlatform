package org.wzy.analyze;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.meta.*;
import org.wzy.method.EntityLinkInter;
import org.wzy.method.elImpl.MaxMatchLinker;
import org.wzy.tool.IOTool;

public class SelectWrongQuestions {
	
	
	public Map<String,double[]> scoreMap;

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
	
	public List<Question> FilterWrongQuestion(List<Question> qList)
	{
		List<Question> wrongList=new ArrayList<Question>();
		
		for(int i=0;i<qList.size();i++)
		{
			double[] scores=scoreMap.get(qList.get(i).questionID);
			int maxindex=0;
			for(int j=1;j<scores.length;j++)
			{
				if(scores[maxindex]<scores[j])
				{
					maxindex=j;
				}
			}
			if(maxindex!=qList.get(i).AnswerKey)
			{
				wrongList.add(qList.get(i));
			}
		}
		
		return wrongList;
	}
	
	public void ReadMap(String filename,String code,Map<String,Integer> entity2id,List<String> id2entity) throws NumberFormatException, IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			entity2id.put(ss[0], Integer.parseInt(ss[1]));
			id2entity.add(ss[0]);
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		SelectWrongQuestions swq=new SelectWrongQuestions();
		swq.scoreMap=swq.ReadScores("D:\\KBQA\\DataSet\\ck12_6000\\5dataset_analysis\\bestresults\\cbiology.path.score");
		String code="utf8";
		
		List<Question> allqList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\ck12_6000\\5dataset_analysis\\en_final_datasets\\cbiology_inf_test.segment.300.concept.exactly", code);
		List<Question> wrongList=swq.FilterWrongQuestion(allqList);
		
		EntityLinkInter linker=new MaxMatchLinker();
		
		Map<String,Integer> entity2id=new HashMap<String,Integer>();
		Map<String,Integer> relation2id=new HashMap<String,Integer>();
		List<String> id2entity=new ArrayList<String>();
		List<String> id2relation=new ArrayList<String>();
		swq.ReadMap("D:\\KBQA\\DataSet\\ck12_6000\\5dataset_analysis\\chineseKb\\entity_in_qa_withindex.db", code, entity2id,id2entity);
		swq.ReadMap("D:\\KBQA\\DataSet\\ck12_6000\\5dataset_analysis\\chineseKb\\chinese_relation_conceptnet.db", code, relation2id,id2relation);		
		linker.SetEntityAndRelationMap(entity2id, relation2id);
		
		for(int i=0;i<wrongList.size();i++)
		{
			wrongList.get(i).LinkingNE(linker);
		}
		
		IOTool.PrintSimpleQuestionsWithConceptPathsWithEntities(wrongList, id2relation, "D:\\KBQA\\DataSet\\ck12_6000\\5dataset_analysis\\wrong_by_path_data\\cbiology.path.wrong", code);
		
		
		
		
		
	}
	
	
}
