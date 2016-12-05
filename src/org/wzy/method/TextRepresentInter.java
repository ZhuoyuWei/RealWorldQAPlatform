package org.wzy.method;

import org.wzy.meta.NNParameter;

public interface TextRepresentInter {

	public double[] RepresentText(double[][] textEmbs,int dim);
	public void SetParameters(NNParameter para);
	//public void SetWordEmbs(double[][] embs);
}
