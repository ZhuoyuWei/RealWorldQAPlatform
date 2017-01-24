package org.wzy.analyze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.meta.Question;
import org.wzy.tool.IOTool;

public class SplitResultFile {

	
	public Map<String,Question> BuildMap(List<Question> qList)
	{
		Map<String,Question> id2question=new HashMap<String,Question>();
		for(int i=0;i<qList.size();i++)
		{
			id2question.put(qList.get(i).questionID, qList.get(i));
		}
		return id2question;
	}
	
	public void SelectAnswer(Map<String,Question> id2question,String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<1)
				continue;
			String[] ss=buffer.split("\t");
			Question qus=id2question.get(ss[0]);
			if(qus!=null)
			{
				ps.println(buffer);
			}
		}
		br.close();
		ps.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		
		SplitResultFile srf=new SplitResultFile();
		
		args=new String[5];
		/*args[0]="D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-Elementary-NDMC-Test.csv.simple";
		args[1]="D:\\KBQA\\DataSet\\ck12_6000\\wrongQ_1.12v_enall.log";		
		args[2]="D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-Elementary-NDMC-Test.csv.simple.lucene_wrong";
		args[3]="D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-MiddleSchool-NDMC-Test.csv.simple";
		args[4]="D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-MiddleSchool-NDMC-Test.csv.simple.lucene_wrong";*/
		
		args[0]="D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\physical-science\\physical-science_qa_12.22v_source.4can";	
		args[1]="D:\\KBQA\\DataSet\\ck12_6000\\wrongQ_1.12v_enall.log";	
		args[2]="D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\physical-science\\physical-science_qa_12.22v_source.4can.lucene_wrong";		
		
		List<Question> qusList=IOTool.ReadSimpleQuestionsCVS(args[0], "utf8");
		Map<String,Question> qmap=srf.BuildMap(qusList);
		srf.SelectAnswer(qmap, args[1], args[2]);

		
		/*qusList=IOTool.ReadSimpleQuestionsCVS(args[3], "utf8");
		qmap=srf.BuildMap(qusList);
		srf.SelectAnswer(qmap, args[1], args[4]);*/

				
		
	}
	
}
