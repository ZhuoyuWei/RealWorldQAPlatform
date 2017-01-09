package org.wzy.method.scImpl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.meta.ConceptPath;
import org.wzy.meta.Question;
import org.wzy.method.KBModel;
import org.wzy.method.ScoringInter;
import org.wzy.method.TrainInter;
import org.wzy.method.elImpl.MaxMatchLinker;
import org.wzy.method.elImpl.NgramEntityLinker;
import org.wzy.method.rwImpl.BiDirectSearchAll;
import org.wzy.method.rwImpl.DFSSearchAll;
import org.wzy.method.rwImpl.RandomExactly;

public class PathCountScorer implements ScoringInter{
	
	public KBModel kbModel;
	


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
			kbm.Kgraph=kbm.ReadTripeltGraph(factFile, "utf8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		kbm.ChangeKGraph2KnGraph(true);
		
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
				kbm.logps=new PrintStream(logfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		System.err.println("Path Count scorer finish  initialization "+(end-start)+"ms");
		
		kbModel=kbm;
	}

	@Override
	public double ScoreQAPair(Question qus, int aindex) {
		// TODO Auto-generated method stub
		
		ConceptPath[] paths=kbModel.MiningPaths(qus, aindex);
		
		double score=0;
		
		for(int i=0;i<paths.length;i++)
		{
			score+=paths[i].count*0.01;
		}
		
		return score;
	}

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double CalLoss(Question q) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean Trainable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TrainInter GetTrainInter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void InitAllGradients() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		
	}

}
