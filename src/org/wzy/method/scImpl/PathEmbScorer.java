package org.wzy.method.scImpl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.wzy.meta.ConceptPath;
import org.wzy.meta.NNParameter;
import org.wzy.meta.Question;
import org.wzy.method.KBModel;
import org.wzy.method.ScoringInter;
import org.wzy.method.TextRepresentInter;
import org.wzy.method.TrainInter;
import org.wzy.method.elImpl.MaxMatchLinker;
import org.wzy.method.elImpl.NgramEntityLinker;
import org.wzy.method.rwImpl.BiDirectSearchAll;
import org.wzy.method.rwImpl.DFSSearchAll;
import org.wzy.method.rwImpl.PRARandomWalk;
import org.wzy.method.rwImpl.RandomExactly;
import org.wzy.method.rwImpl.RandomWalkBasic;
import org.wzy.method.trImpl.LSTMRepresentation;
import org.wzy.method.trImpl.SumRepresentation;
import org.wzy.tool.CoreNLPTool;
import org.wzy.tool.IOTool;
import org.wzy.tool.MatrixTool;

public class PathEmbScorer implements ScoringInter{
	
	public KBModel kbModel;
	
	public TextRepresentInter textpre;
	public TextRepresentInter pathpre;	
	
	//word embedding
	public Map<String,Integer> word2index;
	Set<String> unknownwordSet=new HashSet<String>();
	public double[][] wordEmbs;
	
	//kb embedding
	public double[][] entityEmbs;
	public double[][] relationEmbs;
	
	public int entitySize;
	public int relationSize;
	
	public int dim;
	public double pairwise_margin=1;
	
	public boolean debug_print_concept_paths=false;
	public boolean remove_noun_inqa=false;
	
	public double[] pathWeights;
	public double[] pathGradients;	
	public Map<ConceptPath,Integer> path2index;
	public List<ConceptPath> index2path;
	public boolean weightpath=false;
	
	public Random rand=new Random();
	
	


	//debug by wzy at 12.28
	Map<ConceptPath,Set<String>> path2question=new HashMap<ConceptPath,Set<String>>();
	
	public double[][] Text2Embs(String str)
	{
		List<double[]> embList=new ArrayList<double[]>();
		String[] ss=str.split("[\\s]+");
		int hasword=0;
		for(int i=0;i<ss.length;i++)
		{
			Integer index=word2index.get(ss[i].trim().toLowerCase());
			if(index!=null)
			{
				embList.add(wordEmbs[index]);
				hasword++;
			}
			else
			{
				unknownwordSet.add(ss[i].trim().toLowerCase());
			}
		}
		return embList.toArray(new double[0][]);
	}
	
	public double[][] ConceptPath2Embs(ConceptPath path)
	{
		double[][] embs=new double[path.relationList.size()][];
		for(int i=0;i<embs.length;i++)
		{
			int rel=path.relationList.get(i);
			if(rel>=KBModel.reverseEdge)
			{
				rel-=KBModel.reverseEdge;
				rel+=relationSize;
			}
			try{
			embs[i]=relationEmbs[rel];
			}catch(Exception e)
			{
				System.out.println(rel+"\t"+relationEmbs.length+"\t"+relationSize);
			}
			
		}
		return embs;
	}
	
	public void DoubleDirectRelation()
	{
		double[][] tmp=relationEmbs;
		relationEmbs=new double[tmp.length*2][];
		for(int i=0;i<tmp.length;i++)
		{
			relationEmbs[i]=tmp[i];
			relationEmbs[i+tmp.length]=new double[dim];
			for(int j=0;j<dim;j++)
			{
				relationEmbs[i+tmp.length][j]=-tmp[i][j];
			}
		}
	}
	
	
	@Override
	public void InitScorer(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		String entityFile=paraMap.get("entityFile");
		String relationFile=paraMap.get("relationFile");
		String factFile=paraMap.get("factFile");
		long start=System.currentTimeMillis();
		KBModel kbm=new KBModel();
		try {
			kbm.ReadEntityList(entityFile, "utf8");
			kbm.ReadRelationList(relationFile, "utf8");
			if(factFile!=null)
			{
				kbm.Kgraph=kbm.ReadTripeltGraph(factFile, "utf8");
				kbm.ChangeKGraph2KnGraph(true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		String entitylink=paraMap.get("entitylink");
		switch(entitylink)
		{
		case "ngram":
		{
			kbm.entity_linker=new NgramEntityLinker();
			break;
		}
		case "max":
		{
			kbm.entity_linker=new MaxMatchLinker();
			break;
		}
		}
		kbm.entity_linker.SetEntityAndRelationMap(kbm.entity2id, kbm.relation2id);
		
		String randomwalk=paraMap.get("randomwalk");
		switch(randomwalk)
		{
		case "exactly":
		{
			kbm.random_walker=new RandomExactly();
			break;
		}
		case "pra":
		{
			kbm.random_walker=new PRARandomWalk();
			break;
		}	
		case "rw":
		{
			kbm.random_walker=new RandomWalkBasic();
			break;
		}		
		case "searchall":
		{
			kbm.random_walker=new DFSSearchAll();
			break;
		}
		case "bisearch":
		{
			kbm.random_walker=new BiDirectSearchAll();
			break;
		}
		}	
		kbm.random_walker.SetKGraph(kbm.KnGraph);
		
		long end=System.currentTimeMillis();
		
		
		if(kbm.concept_debug||kbm.ground_debug)
		{
			//String mid2name_file=paraMap.get("mid2name_file");
			String logfile=paraMap.get("logfile");
			try {
				//kbm.ReadEntityNameAndMid(mid2name_file, "utf8");
				if(logfile!=null)
					kbm.logps=new PrintStream(logfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		System.err.println("Path Count scorer finish  initialization "+(end-start)+"ms");
		
		kbModel=kbm;
		
		//for embeddings
		dim=Integer.parseInt(paraMap.get("dim"));
		String word_emb_file=paraMap.get("word_emb_file");
		try {
			if(word_emb_file!=null)
				InitGloveEmbs(word_emb_file,"utf8");
			//InitEmbs(embfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String entity_emb_file=paraMap.get("entity_emb_file");	
		String relation_emb_file=paraMap.get("relation_emb_file");
		if(relation_emb_file!=null)
		{
			relationEmbs=IOTool.ReadDoubleMatrix(relation_emb_file, 2000, dim);
			relationSize=relationEmbs.length;
			this.DoubleDirectRelation();
		}
		else
		{
			relationEmbs=MatrixTool.RandomBuildMatrix(kbm.relationsize, dim, rand);
		}
		
		
		//question representation Model
		String textModel=paraMap.get("textModel");
		switch(textModel)
		{
		case "sum":
		{
			textpre=new SumRepresentation();
			break;
		}
		case "lstm":
		{
			textpre=new LSTMRepresentation();
			break;
		}
		}
		
		//path representation Model
		String pathModel=paraMap.get("pathModel");
		switch(pathModel)
		{
		case "sum":
		{
			pathpre=new SumRepresentation();
			break;
		}
		case "lstm":
		{
			pathpre=new LSTMRepresentation();
			break;
		}
		}		
		
		
		
		textpre.SetParameters(paraMap);
		
	}
	
	/////////////////////////for text embedding/////////////////////////////
	public void InitGloveEmbs(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		word2index=new HashMap<String,Integer>();
		List<double[]> embeddingList=new ArrayList<double[]>();
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split(" ");
			String token=ss[0];
			
			Integer index=word2index.get(token);
			if(index!=null)
				continue;
			
			double[] emb=new double[dim];
			for(int i=0;i<dim;i++)
			{
				emb[i]=Double.parseDouble(ss[i+1]);
			}

			embeddingList.add(emb);
			word2index.put(token,word2index.size());
		}
		br.close();
		
		wordEmbs=embeddingList.toArray(new double[0][]);
		
		if(word2index.size()!=wordEmbs.length)
		{
			System.err.println("there is a different number of words in embedding file.");
			System.exit(-1);
		}
		
	}
	////////////////////////for KB embedding///////////////////////////////
	
	/////////////////////////for scoring/////////////////////////////
	public double SimilarBetweenQuestionAndPath(double[] rep_question,double[] rep_path)
	{
		return MatrixTool.VectorDot(rep_question, rep_path);
	}

	@Override
	public double ScoreQAPair(Question qus, int aindex) {
		// TODO Auto-generated method stub
		
		//for question
		//double[][] res_question_words=Text2Embs(qus.question_content+" "+qus.answers[aindex]);
		double[][] res_question_words=Text2Embs(qus.question_content);
		double[] rep_question=textpre.RepresentText(res_question_words, dim);
		//double[] rep_question=textpre.RepresentText(qus.question_content, dim);
		
		//for concept paths
		//ConceptPath[] paths=kbModel.MiningPaths(qus, aindex);
		ConceptPath[] paths=qus.ans_paths[aindex];
		
		double score=0;
		
		for(int i=0;i<paths.length;i++)
		{
			double[][] path_relations=ConceptPath2Embs(paths[i]);
			double[] rep_path=pathpre.RepresentText(path_relations, dim);
			
			double simi_score=SimilarBetweenQuestionAndPath(rep_question,rep_path);
			//if(simi_score>0)
			
			if(weightpath)
			{
				double weight=pathWeights[path2index.get(paths[i])];
				simi_score*=weight;
			}
			
			score+=simi_score;
		}
		
		return score;
	}

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		
		//random paths for each question and answer pair
		for(int i=0;i<qList.size();i++)
		{
			Question question=qList.get(i);
			question.ans_paths=new ConceptPath[question.answers.length][];
			for(int j=0;j<question.answers.length;j++)
			{
				question.ans_paths[j]=kbModel.MiningPaths(question, j);
			}
		}
		
		if(remove_noun_inqa)
		{
			RemoveNounsinQA(qList);
		}
		
		if(weightpath)
		{
			InitPathWeightRandomly(qList);
		}
		
		//debug by wzy at 12.28
		if(debug_print_concept_paths)
		{
		for(int i=0;i<qList.size();i++)
		{
			Question question=qList.get(i);
			for(int j=0;j<question.answers.length;j++)
			{
				for(int k=0;k<question.ans_paths[j].length;k++)
				{
					Set<String> quesSet=path2question.get(question.ans_paths[j][k]);
					if(quesSet==null)
					{
						quesSet=new HashSet<String>();
						path2question.put(question.ans_paths[j][k], quesSet);
					}
					quesSet.add(question.questionID);
				}
				
			}
		}
		
		Iterator it=path2question.entrySet().iterator();
		//int[] counts=new int[path2question.size()];
		int i=0;
		class ValueAndIndex implements Comparator
		{
			public int value=0;
			public ConceptPath path;
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				
				ValueAndIndex vi1=(ValueAndIndex)o1;
				ValueAndIndex vi2=(ValueAndIndex)o2;
				
				if(Math.abs(vi1.value-vi2.value)<1e-10)
					return 0;
				else if(vi1.value>vi2.value)
					return -1;
				else
					return 1;
			}
		}
		List<ValueAndIndex>  viList=new ArrayList<ValueAndIndex>();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			ConceptPath path=(ConceptPath)entry.getKey();
			Set<String> set=(Set<String>)entry.getValue();
			//counts[i++]=set.size();
			
			if(set.size()<=1)
				continue;
			ValueAndIndex vi=new ValueAndIndex();
			vi.path=path;
			vi.value=set.size();
			viList.add(vi);
		}
		Collections.sort(viList,new ValueAndIndex());
		
		
		for(i=0;i<viList.size();i++)
		{
			System.out.print(viList.get(i).value);
			for(int j=0;j<viList.get(i).path.relationList.size();j++)
			{
				int tmp=viList.get(i).path.relationList.get(j);
				if(tmp>=KBModel.reverseEdge)
				{
					tmp-=KBModel.reverseEdge;
					String rel=this.kbModel.id2relation.get(tmp);
					System.out.print("\t-"+rel);
				}
				else
				{
					String rel=this.kbModel.id2relation.get(tmp);
					System.out.print("\t"+rel);
				}
				
				
			}
			System.out.println();
		}
		
		System.exit(-1);
		}
	}

	@Override
	public double CalLoss(Question q) {
		// TODO Auto-generated method stub
		
		//question embedding
		double[][] res_question_words=Text2Embs(q.question_content);
		double[] rep_question=textpre.RepresentText(res_question_words, dim);
		double[] neg_repq=new double[rep_question.length];

		for(int j=0;j<neg_repq.length;j++)
		{
			neg_repq[j]=-rep_question[j];
		}
		//pairwise
		double[] scores=new double[q.answers.length];		
		ConceptPath[][] paths=new ConceptPath[q.answers.length][];
		
		double[][][][] path_relations=new double[paths.length][][][];
		double[][][] rep_path=new double[paths.length][][];
		for(int i=0;i<paths.length;i++)
		{
			
			//paths[i]=kbModel.MiningPaths(q, i);
			paths[i]=q.ans_paths[i];		
			
			path_relations[i]=new double[paths[i].length][][];
			rep_path[i]=new double[paths[i].length][];
			
			for(int j=0;j<paths[i].length;j++)
			{
				path_relations[i][j]=ConceptPath2Embs(paths[i][j]);
				rep_path[i][j]=pathpre.RepresentText(path_relations[i][j], dim);
				double simi_score=SimilarBetweenQuestionAndPath(rep_question,rep_path[i][j]);
				if(weightpath)
				{
					double weight=pathWeights[path2index.get(paths[i][j])];
					simi_score*=weight;
				}
				scores[i]+=simi_score;
			}
		}
		
		
		if(scores.length>1)
		{
			//pair wise loss
			for(int i=0;i<scores.length;i++)
			{
				if(i==q.AnswerKey)
					continue;
				double paircost=MatrixTool.PairWiseMargin(scores[q.AnswerKey],scores[i],pairwise_margin);
				if(paircost<1e-10)
					continue;
				
				TrainInter path_traininter=(TrainInter)pathpre;
				TrainInter text_traininter=(TrainInter)textpre;
				
				
				if(weightpath)
				{
					//for right answer
					for(int j=0;j<paths[q.AnswerKey].length;j++)
					{
						ConceptPath path=paths[q.AnswerKey][j];
						int pathindex=path2index.get(path);
						double weight=pathWeights[pathindex];
						
						double[] neg_repq_weighted=new double[neg_repq.length];
						for(int k=0;k<dim;k++)
						{
							neg_repq_weighted[k]=weight*neg_repq[k];
						}
						
						path_traininter.CalculateGradient(path_relations[q.AnswerKey][j], neg_repq_weighted);
						
						double[] neg_path_weighted=new double[dim];
						for(int k=0;k<dim;k++)
						{
							neg_path_weighted[k]=-rep_path[q.AnswerKey][j][k]*weight;
						}
						text_traininter.CalculateGradient(res_question_words, neg_path_weighted);
						
						//for path weight
						double simi_score=SimilarBetweenQuestionAndPath(rep_question,rep_path[q.AnswerKey][j]);
						pathGradients[pathindex]-=simi_score;
					}
					
				
					
					//for wrong answer
					for(int j=0;j<paths[i].length;j++)
					{
						ConceptPath path=paths[i][j];
						int pathindex=path2index.get(path);
						double weight=pathWeights[pathindex];
						double[] rep_question_weighted=new double[rep_question.length];
						for(int k=0;k<dim;k++)
						{
							rep_question_weighted[k]=weight*rep_question[k];
						}
						path_traininter.CalculateGradient(path_relations[i][j], rep_question_weighted);
						double[] rep_path_weighted=new double[rep_path[i][j].length];
						for(int k=0;k<dim;k++)
						{
							rep_path_weighted[k]=weight*rep_path[i][j][k];
						}						
						text_traininter.CalculateGradient(res_question_words, rep_path_weighted);
						
						//for path weight
						double simi_score=SimilarBetweenQuestionAndPath(rep_question,rep_path[i][j]);
						pathGradients[pathindex]+=simi_score;
					}	
				}
				else
				{
					//for right answer
					for(int j=0;j<paths[q.AnswerKey].length;j++)
					{
						ConceptPath path=paths[q.AnswerKey][j];
						path_traininter.CalculateGradient(path_relations[q.AnswerKey][j], neg_repq);
						
						double[] neg_path=new double[dim];
						for(int k=0;k<dim;k++)
						{
							neg_path[k]=-rep_path[q.AnswerKey][j][k];
						}
						text_traininter.CalculateGradient(res_question_words, neg_path);
					}
					
				
					
					//for wrong answer
					for(int j=0;j<paths[i].length;j++)
					{
						ConceptPath path=paths[i][j];
						path_traininter.CalculateGradient(path_relations[i][j], rep_question);
						text_traininter.CalculateGradient(res_question_words, rep_path[i][j]);
					}				
				}
					
					
				
				
				
				//for question
				/*double[] ans_minus_embs=new double[qus_embs.length];
				for(int j=0;j<qus_embs.length;j++)
				{
					ans_minus_embs[j]=ans_embs[i][j]-ans_embs[q.AnswerKey][j];
				}
				trainInter.CalculateGradient(qus_words_embs, ans_minus_embs);*/
			}
			
			//point wise loss, added by wzy at 2017.1.5
			/*for(int i=0;i<scores.length;i++)
			{
				TrainInter path_traininter=(TrainInter)pathpre;
				TrainInter text_traininter=(TrainInter)textpre;
				if(i==q.AnswerKey)
				{
					//for right answer
					for(int j=0;j<paths[q.AnswerKey].length;j++)
					{
						ConceptPath path=paths[q.AnswerKey][j];
						path_traininter.CalculateGradient(path_relations[q.AnswerKey][j], neg_repq);
						
						double[] neg_path=new double[dim];
						for(int k=0;k<dim;k++)
						{
							neg_path[k]=-rep_path[q.AnswerKey][j][k];
						}
						text_traininter.CalculateGradient(res_question_words, neg_path);
					}
				}
				else
				{
					//for wrong answer
					for(int j=0;j<paths[i].length;j++)
					{
						ConceptPath path=paths[i][j];
						path_traininter.CalculateGradient(path_relations[i][j], rep_question);
						text_traininter.CalculateGradient(res_question_words, rep_path[i][j]);
					}	
				}
			}*/
			
		}
		return 0;
	}

	@Override
	public boolean Trainable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public TrainInter GetTrainInter() {
		// TODO Auto-generated method stub
		return (TrainInter)this.textpre;
	}

	@Override
	public void InitAllGradients() {
		// TODO Auto-generated method stub
		((TrainInter)(this.textpre)).InitGradients();
		((TrainInter)(this.pathpre)).InitGradients();
		if(weightpath)
		{
			pathGradients=new double[pathWeights.length];
		}
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		((TrainInter)(this.textpre)).UpgradeGradients(gamma);
		((TrainInter)(this.pathpre)).UpgradeGradients(gamma);
		if(weightpath)
		{
			this.UpgradePathWeightGradients(gamma);
		}
		//ProjectL2();
	}
	
	public void ProjectL2()
	{
		MatrixTool.ProjectL2ByRow(relationEmbs);
		MatrixTool.ProjectL2ByRow(wordEmbs);		
	}
	

	public void RemoveNounsinQA(List<Question> qList)
	{
		long start=System.currentTimeMillis();
		if(CoreNLPTool.UniqueObject==null)
		{
			CoreNLPTool.CreateUniqueObject();
			CoreNLPTool.UniqueObject.InitTool("tokenize, ssplit, pos");
		}
		for(int i=0;i<qList.size();i++)
		{
			qList.get(i).question_content=CoreNLPTool.UniqueObject.RemoveNouns(qList.get(i).question_content);
			for(int j=0;j<qList.get(i).ans_paths.length;j++)
			{
				qList.get(i).answers[j]=CoreNLPTool.UniqueObject.RemoveNouns(qList.get(i).answers[j]);
			}
		}
		long end=System.currentTimeMillis();
		System.err.println("remove nouns from question and answers takes "+(end-start)+" ms");
	}
	
	public void InitPathWeightRandomly(List<Question> qList)
	{
		path2index=new HashMap<ConceptPath,Integer>();
		index2path=new ArrayList<ConceptPath>();
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			for(int j=0;j<q.ans_paths.length;j++)
			{
				for(int k=0;k<q.ans_paths[j].length;k++)
				{
					Integer index=path2index.get(q.ans_paths[j][k]);
					if(index==null)
					{
						path2index.put(q.ans_paths[j][k], index2path.size());
						index2path.add(q.ans_paths[j][k]);
					}
				}
			}
		}
		System.err.println("Concept path total number: "+index2path.size());
		pathWeights=new double[index2path.size()];
		for(int i=0;i<pathWeights.length;i++)
		{
			//pathWeights[i]=rand.nextDouble();
			pathWeights[i]=0;
		}
	}
	
	public void UpgradePathWeightGradients(double gamma)
	{
		for(int i=0;i<pathWeights.length;i++)
		{
			if(Math.abs(pathGradients[i])<1e-6)
				continue;
			pathWeights[i]-=gamma*(pathGradients[i]);
		}
	}
	
	public void PrintPathWeights(String filename)
	{
		if(!weightpath)
			return;
		PrintStream ps=null;
		try {
			ps = new PrintStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0;i<pathWeights.length;i++)
		{
			ps.print(pathWeights[i]+"\t");
		}
		ps.close();
	}
	
	private ConceptPath[] ReAllocatePaths(ConceptPath[] s,boolean[] flags)
	{
		List<ConceptPath> pathList=new ArrayList<ConceptPath>();
		for(int i=0;i<flags.length;i++)
		{
			if(!flags[i])
			{
				pathList.add(s[i]);
			}
		}
		return pathList.toArray(new ConceptPath[0]);
	}
	
	public void RemovePathsUseless(List<Question> qList)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			Map<ConceptPath,Integer> ansPathMap=new HashMap<ConceptPath,Integer>();
			for(int j=0;j<q.ans_paths[q.AnswerKey].length;j++)
			{
				ansPathMap.put(q.ans_paths[q.AnswerKey][j], j);
			}
			boolean[] key_useless=new boolean[q.ans_paths[q.AnswerKey].length];
			
			for(int j=0;j<q.ans_paths.length;j++)
			{
				if(j==q.AnswerKey)
					continue;
				boolean[] ans_useless=new boolean[q.ans_paths[j].length];
				for(int k=0;k<q.ans_paths[j].length;k++)
				{
					Integer index=ansPathMap.get(q.ans_paths[j][k]);
					if(index!=null)
					{
						key_useless[index]=true;
						ans_useless[k]=true;
					}
				}
				
				q.ans_paths[j]=ReAllocatePaths(q.ans_paths[j],ans_useless);
			}
			q.ans_paths[q.AnswerKey]=ReAllocatePaths(q.ans_paths[q.AnswerKey],key_useless);
		}
	}
	
	public void PickRightPaths2Questions(List<Question> goldList,List<Question> testList,int[] test_res,String outputfile)
	{
		Map<ConceptPath,List<Integer>> path2rq=new HashMap<ConceptPath,List<Integer>>();
		for(int i=0;i<testList.size();i++)
		{
			Question q=testList.get(i);
			for(int j=0;j<q.ans_paths[q.AnswerKey].length;j++)
			{
				List<Integer> indexList=path2rq.get(q.ans_paths[q.AnswerKey][j]);
				if(indexList==null)
				{
					indexList=new ArrayList<Integer>();
					path2rq.put(q.ans_paths[q.AnswerKey][j], indexList);
				}
				indexList.add(i);
			}
		}
		
		List<ConceptPath> pathList=new ArrayList<ConceptPath>();
		List<List<Integer>> testIndexList=new ArrayList<List<Integer>>();
		List<Integer> goldIndexList=new ArrayList<Integer>();
		
		for(int i=0;i<goldList.size();i++)
		{
			Question q=testList.get(i);
			for(int j=0;j<q.ans_paths[q.AnswerKey].length;j++)
			{
				List<Integer> indexList=path2rq.get(q.ans_paths[q.AnswerKey][j]);
				if(indexList!=null)
				{
					pathList.add(q.ans_paths[q.AnswerKey][j]);
					testIndexList.add(indexList);
					goldIndexList.add(i);
				}
			}
		}
		
		Set<Integer> goldIndexSet=new HashSet<Integer>();
		goldIndexSet.addAll(goldIndexList);
		System.err.println("gold concept paths: "+pathList.size()+" questions: "+goldIndexSet.size());
		
		if(outputfile!=null)
		{
			try {
				PrintWriter pw=new PrintWriter(outputfile,"utf8");
				
				for(int i=0;i<pathList.size();i++)
				{
					//print paths
					ConceptPath path=pathList.get(i);
					for(int j=0;j<path.relationList.size();j++)
					{
						int relindex=path.relationList.get(j);
						if(relindex>=KBModel.reverseEdge)
						{
							pw.print("-"+kbModel.id2relation.get(relindex-KBModel.reverseEdge)+"\t");
						}
						else
						{
							pw.print(kbModel.id2relation.get(relindex)+"\t");
						}
						
					}
					pw.println();
					
					//print gold question
					IOTool.PrintOneSimpleQuestion(goldList.get(goldIndexList.get(i)), pw);
					pw.println("******************************************************");
					for(int j=0;j<testIndexList.get(i).size();j++)
					{
						if(test_res[testIndexList.get(i).get(j)]==testList.get(testIndexList.get(i).get(j)).AnswerKey)
							pw.print("Right\t");
						else
							pw.print("Wrong\t"+test_res[testIndexList.get(i).get(j)]+"\t");
						IOTool.PrintOneSimpleQuestion(testList.get(testIndexList.get(i).get(j)), pw);
					}
					pw.println();
				}
				
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	

}

