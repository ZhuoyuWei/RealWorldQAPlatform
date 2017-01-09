package org.wzy.pre;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wzy.meta.NELink;
import org.wzy.meta.Question;
import org.wzy.method.KBModel;
import org.wzy.method.elImpl.MaxMatchLinker;
import org.wzy.method.elImpl.NgramEntityLinker;
import org.wzy.tool.IOTool;

public class CollectAllNamesOccuseInQuestions {

	public Set<String> namesInQuestions=new HashSet<String>();
	public KBModel kbm;
	public void Collecting(List<Question> qList)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question question=qList.get(i);
			List<NELink> linkList=kbm.entity_linker.LinkingString(question.question_content);
			for(int j=0;j<question.answers.length;j++)
			{
				List<NELink> ansList=kbm.entity_linker.LinkingString(question.answers[j]);
				linkList.addAll(linkList);
			}
			for(int j=0;j<linkList.size();j++)
			{
				this.namesInQuestions.add(linkList.get(j).source_str);
			}
		}
	}
	
	public void PrintSet(String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		String[] names=this.namesInQuestions.toArray(new String[0]);
		PrintWriter pw=new PrintWriter(filename,"utf8");
		for(int i=0;i<names.length;i++)
		{
			pw.println(names[i]);
		}
		pw.flush();
		pw.close();
	}
	

	
	public static void main(String[] args) throws IOException
	{
		CollectAllNamesOccuseInQuestions caoiq=new CollectAllNamesOccuseInQuestions();
		
		caoiq.kbm=new KBModel();
		long start=System.currentTimeMillis();
		caoiq.kbm.ReadEntityListSecondCol(args[0], "utf8",0);
		long end=System.currentTimeMillis();
		System.out.println("read is ending in "+(end-start)+"ms");
		
		//caoiq.kbm.entity_linker=new MaxMatchLinker();
		caoiq.kbm.entity_linker=new NgramEntityLinker();		
		caoiq.kbm.entity_linker.SetEntityAndRelationMap(caoiq.kbm.entity2id, null);
		
		List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[1], "utf8");
		
		caoiq.Collecting(questionList);
		
		caoiq.PrintSet(args[2]);
		
		
		
	}
	
}
