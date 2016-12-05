package org.wzy.pre;

import org.wzy.tool.CoreNLPTool;
import org.wzy.tool.IOTool;

import edu.stanford.nlp.util.CoreMap;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.*;

public class ReadAndPrintQAGrammer {

	
	public List<Question> questionList;
	public String stan_pipeline="tokenize, ssplit, pos, lemma, depparse";
	
	public void GetQuestionGrammer()
	{
		for(int i=0;i<questionList.size();i++)
		{
			questionList.get(i).CoreNLPProcessing();
		}
	}
	
	public void PrintQuestionDependency(String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<questionList.size();i++)
		{
			String que_str=questionList.get(i).GetGrammerString();
			pw.println(que_str);
		}
		pw.flush();
		pw.close();
		
	}
	
	public void PrintQACoreMap(ObjectOutputStream outobj)
	{
		for(int i=0;i<questionList.size();i++)
		{
			questionList.get(i).SerializGrammerToStream(outobj);
		}		
	}
	
	public List<CoreMap> testRead(String filename) throws IOException, ClassNotFoundException
	{
		List<CoreMap> cmList=new ArrayList<CoreMap>();
		FileInputStream fin=new FileInputStream(filename);
		ObjectInputStream ofin=new ObjectInputStream(fin);
		CoreMap cm=null;
		try{
			while((cm=(CoreMap)ofin.readObject())!=null)
			{
				cmList.add(cm);
			}
			ofin.close();
		}catch(EOFException e)
		{
			ofin.close();
		}
		return cmList;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		String inputfile="D:\\KBQA\\DataSet\\ck12_6000\\mcqa_11.30v.little.head100";
		String outputfile="D:\\KBQA\\DataSet\\ck12_6000\\mcqa_11.30v.little.head100.dep";
		String code="ascii";
		
		ReadAndPrintQAGrammer rp=new ReadAndPrintQAGrammer();
		rp.questionList=IOTool.ReadSimpleQuestionsCVS(inputfile,code);
		CoreNLPTool.CreateUniqueObject();
		CoreNLPTool.UniqueObject.InitTool(rp.stan_pipeline);
		rp.GetQuestionGrammer();
		//rp.PrintQuestionDependency(outputfile,code);
		
		FileOutputStream fos=new FileOutputStream(outputfile);
		ObjectOutputStream outobj=new ObjectOutputStream(fos);
		rp.PrintQACoreMap(outobj);
		outobj.close();
		
		List<CoreMap> tmpcmList=rp.testRead(outputfile);
		System.out.println(tmpcmList.size());
		
	}


}
