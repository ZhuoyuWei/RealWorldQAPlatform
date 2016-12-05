package org.wzy.clus;

import java.util.List;

import org.wzy.meta.Question;

public interface QAClusterInter {

	
	public void Clustering(List<Question> questionList);
	public void ProduceClusterFeatures(List<Question> questionList);
}
