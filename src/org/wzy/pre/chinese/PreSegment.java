package org.wzy.pre.chinese;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wzy.meta.Question;
import org.wzy.tool.IOTool;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

public class PreSegment {

	public Seg seg;
	public Map<String,Integer> token2id=new HashMap<String,Integer>();
	public List<String> id2token=new ArrayList<String>();
	
	
	
	public String SegmetnText(String text) throws IOException
	{
		MMSeg mmSeg = new MMSeg(new StringReader(text), seg); 
		Word word = null;
		StringBuilder sb=new StringBuilder();
		while((word = mmSeg.next())!=null)
		{
			sb.append(word.getString());
			sb.append(" ");
		} 
		return sb.toString().trim();
	}
	
	public void FilterWordslist(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),"utf8"));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,"utf8");
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			pw.println(buffer);
		}
		pw.flush();
		pw.close();
		br.close();
	}
	
	public void CollectNameFromText(String text)
	{
		String[] ss=text.split("[\\s]+");
		for(int i=0;i<ss.length;i++)
		{
			Integer index=token2id.get(ss[i]);
			if(index==null)
			{
				token2id.put(ss[i], id2token.size());
				id2token.add(ss[i]);
			}
		}
	}
	
	public void CollectToken(List<Question> qList)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			CollectNameFromText(q.question_content);
			for(int j=0;j<q.answers.length;j++)
			{
				CollectNameFromText(q.answers[j]);
			}
		}		
		System.out.println("token size "+token2id.size());
	}
	
	public void PrintOutTokenList(String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,"utf8");
		for(int i=0;i<id2token.size();i++)
		{
			pw.println(id2token.get(i));
		}
		pw.flush();
		pw.close();
	}
	
	
	public void FilterOutEmbeddings(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),"utf8"));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,"utf8");
		int line=0;
		while((buffer=br.readLine())!=null)
		{
			if(line==0)
			{
				line++;
				continue;
			}
			String[] ss=buffer.split("[\\s]+");
			if(ss.length!=101)
			{
				//System.err.println(buffer);
				continue;
			}
			
			String token=ss[0];
			if(token2id.containsKey(token))
			{
				pw.println(buffer);
			}
			
		}
		pw.flush();
		pw.close();
		br.close();
	}
	
	
	public static void CollectTokenMain() throws FileNotFoundException, UnsupportedEncodingException
	{
		PreSegment ps=new PreSegment();

		List<Question> qList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolBiology\\cbiology_inf_train.segment.concept.exactly", "utf8");
		qList.addAll(IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolBiology\\cbiology_inf_test.segment.concept.exactly", "utf8"));
		qList.addAll(IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolHistory\\chistory_inf_test.segment.concept.exactly", "utf8"));
		qList.addAll(IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolHistory\\chistory_inf_test.segment.concept.exactly", "utf8"));	
		
		ps.CollectToken(qList);
		ps.PrintOutTokenList("D:\\KBQA\\DataDump\\chineseKb\\chinese_token_all_1.15v.db");		
	}
	
	public static void main(String[] args) throws IOException
	{
		PreSegment ps=new PreSegment();
		/*File file = new File("E:\\Users\\Administrator\\workspace\\AI2QA\\data\\words.list");
		System.out.println(file.getAbsolutePath());
		Dictionary dic = Dictionary.getInstance(file);
		ps.seg=new ComplexSeg(dic);
		
		List<Question> qList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolBiology\\cbiology_inf_test.csv.concept.exactly", "utf8");
		
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			q.question_content=ps.SegmetnText(q.question_content);
			for(int j=0;j<q.answers.length;j++)
			{
				q.answers[j]=ps.SegmetnText(q.answers[j]);
			}
		}
		
		IOTool.PrintSimpleQuestionsWithConceptPaths(qList, "D:\\KBQA\\DataSet\\MiddleSchoolBiology\\cbiology_inf_test.segment.concept.exactly", "utf8");
		*/
		
		//ps.FilterWordslist("E:\\Users\\Administrator\\workspace\\AI2QA\\data\\words.list", "E:\\Users\\Administrator\\workspace\\AI2QA\\data\\words.filted.list");
		
		//CollectTokenMain();

		List<Question> qList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolBiology\\cbiology_inf_train.segment.concept.exactly", "utf8");
		qList.addAll(IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolBiology\\cbiology_inf_test.segment.concept.exactly", "utf8"));
		qList.addAll(IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolHistory\\chistory_inf_test.segment.concept.exactly", "utf8"));
		qList.addAll(IOTool.ReadSimpleQuestionsCVSWithConceptPaths("D:\\KBQA\\DataSet\\MiddleSchoolHistory\\chistory_inf_test.segment.concept.exactly", "utf8"));	
		
		ps.CollectToken(qList);		
		ps.FilterOutEmbeddings("D:\\KBQA\\DataDump\\baike-embeddings.txt", "D:\\KBQA\\DataDump\\chineseKb\\chinese_words.emb");
		

	}
	
}
