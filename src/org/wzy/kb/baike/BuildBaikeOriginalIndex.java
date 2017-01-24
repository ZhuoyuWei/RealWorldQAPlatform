package org.wzy.kb.baike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.wzy.tool.CoreNLPTool;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;

public class BuildBaikeOriginalIndex {
	
	public QueryParser parser=null;
	//public IndexSearcher searcher=null;
	public IndexWriterConfig iwc=null;
	public IndexWriter writer = null;
	public boolean InitLucene(String indexdir,String code)
	{
		try {
			//reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexdir)));
			//searcher = new IndexSearcher(reader);
			
			Reader stopwordReader=new BufferedReader(new InputStreamReader(new FileInputStream("../data/stopwords_cn.txt"),code));
			//Analyzer analyzer=new StandardAnalyzer(stopwordReader);
			Analyzer analyzer=new ComplexAnalyzer();
			String field = "contents";
			parser = new QueryParser(field, analyzer);
			parser.setAllowLeadingWildcard(true);
			iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(FSDirectory.open(Paths.get(indexdir)), iwc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	void indexDoc(IndexWriter writer, String doctext, long lastModified,String code) throws IOException 
	{	
		Document doc = new Document();
		doc.add(new LongPoint("modified", lastModified));
		doc.add(new TextField("contents", doctext,Field.Store.YES));
		if (writer.getConfig().getOpenMode() == OpenMode.CREATE)
		{
			writer.addDocument(doc);
		} 
	}

	public void ReadBaikeBigFileAndIndex(String inputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		//PrintWriter pw=new PrintWriter(outputfile,"utf8");
		String buffer=null;
		StringBuilder sb=new StringBuilder();
		String lasttitle=null;
		int count=0;
		while((buffer=br.readLine())!=null)
		{
			String pattern="<lemmatitle>(.*?)</lemmatitle>";
			Pattern p=Pattern.compile(pattern);
			Matcher matcher=p.matcher(buffer);
			if(matcher.find())
			{
				String title=matcher.group(1);
				title=title.toLowerCase().replaceAll("[\\s]+", "_");
				//titleList.add(title);
				//pw.println(title);
				
				if(lasttitle!=null)
				{
					//for lucene indexing one page.
					String text=sb.toString();
					indexDoc(writer, text, System.currentTimeMillis(),code);
				}
				sb=new StringBuilder();
				lasttitle=title;
				sb.append(buffer);
			}
			else
			{
				sb.append(buffer);
				sb.append("\n");
			}
			count++;
			//if(count==1000)
				//break;
		}
		System.out.println("Total index "+count+" pages");
		br.close();
		writer.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		long start=System.currentTimeMillis();
		BuildBaikeOriginalIndex bw=new BuildBaikeOriginalIndex();
		bw.InitLucene(args[0],"utf8");
		bw.ReadBaikeBigFileAndIndex(args[1],"gb2312");
		long end=System.currentTimeMillis();
		System.err.println("Index pages in "+(end-start)+" ms");
	}
}
