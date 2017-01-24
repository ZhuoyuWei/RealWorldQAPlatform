package org.wzy.clus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.meta.*;
import org.wzy.method.trImpl.SumRepresentation;
import org.wzy.tool.IOTool;

public class ProduceEmbeddingClusterData {

	public List<Question> qList;
	
	public SumRepresentation sr=new SumRepresentation();
	
	public void InitSR(String embfile,int dim)
	{
		Map<String,Integer> word2index=new HashMap<String,Integer>();
		double[][] wordEmbs=null;
		if(embfile!=null)
		{
			wordEmbs=sr.InitGloveEmbs(embfile,"utf8",word2index,dim);
		}
		sr.SetEmbeddings(wordEmbs);
		sr.SetWord2Index(word2index);
		sr.SetDim(dim);
	}
	
	public void ProduceEmbedding(List<Question> qList)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			double[] q_con_embs=sr.RepresentText(q.question_content, sr.dim);
		}
	}
	
	public static void main(String[] args)
	{
		ProduceEmbeddingClusterData pec=new ProduceEmbeddingClusterData();
		pec.qList=IOTool.ReadSimpleQuestionsCVS(args[0], "utf8");
		pec.InitSR(args[1], 100);
		
		
	}
}
