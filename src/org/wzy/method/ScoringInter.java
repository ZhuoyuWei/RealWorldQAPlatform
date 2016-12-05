package org.wzy.method;

import java.util.Map;

import org.wzy.meta.Question;

public interface ScoringInter {

	public void InitScorer(Map<String,String> paraMap);
	public double ScoreQAPair(Question qus,int aindex);
	
}
