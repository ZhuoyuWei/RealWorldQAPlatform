package org.wzy.method;

import org.wzy.meta.NNParameter;

public class SumRepresentation implements TextRepresentInter{


	@Override
	public double[] RepresentText(double[][] textEmbs,int dim) {
		// TODO Auto-generated method stub
		double[] res=new double[dim];
		for(int i=0;i<textEmbs.length;i++)
		{
			for(int j=0;j<dim;j++)
			{
				res[j]+=textEmbs[i][j];
			}
		}
		
		if(textEmbs.length>1)
		{
			for(int i=0;i<dim;i++)
			{
				res[i]/=textEmbs.length;
			}
		}
		
		return res;
	}

	@Override
	public void SetParameters(NNParameter para) {
		// TODO Auto-generated method stub
		
	}



}
