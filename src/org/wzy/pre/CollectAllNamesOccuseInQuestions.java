package org.wzy.pre;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wzy.kb.cn.FilterOutChineseTriplet;
import org.wzy.meta.NELink;
import org.wzy.meta.Question;
import org.wzy.method.KBModel;
import org.wzy.method.elImpl.ChineseMaxMatchLinker;
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
			if(FilterOutChineseTriplet.isContainChinese(names[i]))
				pw.println(names[i]);
		}
		pw.flush();
		pw.close();
	}
	

	public void ReadEntityMapAndIndex(String filename,String code,String inputfile,String outputfile) throws IOException
	{
		Map<String,Integer> entity2id=new HashMap<String,Integer>();
		String buffer=null;
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			entity2id.put(ss[0], Integer.parseInt(ss[1]));
		}
		br.close();
		
		buffer=null;
		br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		PrintWriter pw=new PrintWriter(outputfile,code);
		while((buffer=br.readLine())!=null)
		{
			Integer index=entity2id.get(buffer);
			if(index==null)
			{
				System.err.println("error: "+buffer);
			}
			pw.println(buffer+"\t"+index);
		}
		pw.flush();
		pw.close();
		br.close();
		
	}
	
	public static void main(String[] args) throws IOException
	{
		CollectAllNamesOccuseInQuestions caoiq=new CollectAllNamesOccuseInQuestions();
		
		caoiq.kbm=new KBModel();
		long start=System.currentTimeMillis();
		caoiq.kbm.ReadEntityListSecondCol(args[0], "utf8",0);
		//caoiq.kbm.ReadEntityListSecondCol("D:\\KBQA\\DataDump\\chineseKb\\", "gb2312",0);
		long end=System.currentTimeMillis();
		System.out.println("read is ending in "+(end-start)+"ms");
		
		//caoiq.kbm.entity_linker=new MaxMatchLinker();
		//caoiq.kbm.entity_linker=new NgramEntityLinker();	
		caoiq.kbm.entity_linker=new ChineseMaxMatchLinker();
		caoiq.kbm.entity_linker.SetEntityAndRelationMap(caoiq.kbm.entity2id, null);
		
		List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[1], "utf8");
		
		caoiq.Collecting(questionList);
		
		caoiq.PrintSet(args[2]);
		
		//caoiq.ReadEntityMapAndIndex("D:\\KBQA\\DataDump\\chineseKb\\chinese_entity_conceptnet.db", "utf8", "D:\\KBQA\\DataDump\\chineseKb\\entity_in_qa.db", "D:\\KBQA\\DataDump\\chineseKb\\entity_in_qa_withindex.db");
		
		
		
	}
	
}
