package org.wzy.method.trImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jblas.DoubleMatrix;
import org.wzy.method.TextRepresentInter;
import org.wzy.method.TrainInter;

import com.lipiji.mllib.layers.MatIniter;
import com.lipiji.mllib.rnn.gru.GRU;
import com.lipiji.mllib.rnn.lstm.Cell;

public class GRURepresentation implements TextRepresentInter,TrainInter{
	
	public GRU gru;
	public int dim;
	public Map<String,Integer> word2index;
	public double[][] embeddings;  
	public Set<String> unknownwordSet=new HashSet<String>();
	public static boolean update_wordembs=true;

	@Override
	public double[] RepresentText(double[][] textEmbs, int dim) {
		// TODO Auto-generated method stub
		
		DoubleMatrix[] textDms=doubles2DMs(textEmbs);
		DoubleMatrix dm_res=RepresentText(textDms);
		
		return dm_res.toArray();
	}

	@Override
	public double[] RepresentText(String str, int dim) {
		// TODO Auto-generated method stub
		double[][] token_embs=Text2Embs(str);
		return RepresentText(token_embs,dim);
	}

	@Override
	public void SetParameters(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		int inSize=Integer.parseInt(paraMap.get("inSize"));
		int outSize=Integer.parseInt(paraMap.get("outSize"));
		
		int typeLabel=Integer.parseInt(paraMap.get("typeLabel"));
		MatIniter.Type type=typeLabel==0?MatIniter.Type.Uniform:MatIniter.Type.Gaussian;
		System.out.println("Gru's type is "+type+"\t"+typeLabel);
		double scale=Double.parseDouble(paraMap.get("scale"));
		double miu=Double.parseDouble(paraMap.get("miu"));
		double sigma=Double.parseDouble(paraMap.get("sigma"));
		
		//lstm=new Cell(inSize,outSize,new MatIniter(type,scale,miu,sigma));
		gru=new GRU(inSize, outSize, new MatIniter(type,scale,miu,sigma));
		
		String paraFile=paraMap.get("paraFile");
		if(paraFile!=null)
		{
			//read learned parameters from file
		}
		
	}

	@Override
	public void SetEmbeddings(double[][] embeddings) {
		// TODO Auto-generated method stub
		this.embeddings=embeddings;
	}
	@Override
	public void SetWord2Index(Map<String, Integer> word2index) {
		// TODO Auto-generated method stub
		this.word2index=word2index;
	}

	@Override
	public void SetDim(int dim) {
		// TODO Auto-generated method stub
		this.dim=dim;
	}

	@Override
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
				embList.add(embeddings[index]);
				hasword++;
			}
			else
			{
				unknownwordSet.add(ss[i].trim().toLowerCase());
			}
		}
		return embList.toArray(new double[0][]);
	}
	@Override
	public List<Integer> Text2Index(String str) {
		// TODO Auto-generated method stub
		List<Integer> indexList=new ArrayList<Integer>();
		String[] ss=str.split("[\\s]+");
		for(int i=0;i<ss.length;i++)
		{
			Integer index=word2index.get(ss[i].trim().toLowerCase());
			if(index!=null)
			{
				indexList.add(index);
				
			}
			
		}
		return indexList;	
	}

	@Override
	public void InitGradients() {
		// TODO Auto-generated method stub
		gru.InitGradients();
		gru.gwords=new ArrayList<double[][]>();
		gru.swords=new ArrayList<double[][]>();
	}

	@Override
	public void CalculateGradient(double[][] words_embs, double[] loss) {
		// TODO Auto-generated method stub
		
		if(words_embs==null||words_embs.length<=0)
			return;
		
		if(this.update_wordembs)
		{
			gru.swords.add(words_embs);
		}
		
		DoubleMatrix[] words_dms=doubles2DMs(words_embs);
		DoubleMatrix loss_dms=(new DoubleMatrix(loss)).transpose();
		
		//forward
		DoubleMatrix[] xs=words_dms;
		DoubleMatrix[] hs=new DoubleMatrix[words_dms.length];
		DoubleMatrix[] rs=new DoubleMatrix[words_dms.length];
		DoubleMatrix[] zs=new DoubleMatrix[words_dms.length]; 
		DoubleMatrix[] ghs=new DoubleMatrix[words_dms.length];
		
		for(int i=0;i<words_dms.length;i++)
		{
			gru.active(xs,hs,rs,zs,ghs,i);
		}
		
		
		/*DoubleMatrix[][] states=new DoubleMatrix[words_embs.length][];
		for(int i=0;i<words_dms.length;i++)
		{
			DoubleMatrix preH=null;
			DoubleMatrix preC=null;
			if(i>0)
			{
				preH=states[i-1][6];
				preC=states[i-1][3];
			}
			states[i]=lstm.active(i, words_dms[i],preH,preC);
		}*/
		
		
		//backward
		/*if(lstm.update_wordembs)
		{
			lstm.swords.add(words_embs);
		}*/
		
		gru.bptt(xs, hs, rs, zs, ghs, words_dms.length-1, loss_dms);
		
		//lstm.bptt(states, words_dms, words_dms.length-1, loss_dms);
	}

	@Override
	public void CalculateGradient(String text, double[] loss) {
		// TODO Auto-generated method stub
		double[][] wordembs=Text2Embs(text);
		CalculateGradient(wordembs,loss);
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		gru.UpdateParameters(gamma);
		
	}

	
	
	
	///////////////////////////////////////
	
	
	
	public DoubleMatrix[] doubles2DMs(double[][] textEmbs)
	{
		DoubleMatrix[] textDms=new DoubleMatrix[textEmbs.length];
		for(int i=0;i<textEmbs.length;i++)
		{
			textDms[i]=(new DoubleMatrix(textEmbs[i])).transpose();
			//System.out.println("debug "+textDms[i].rows+" "+textDms[i].columns+" "+textEmbs[i].length);
		}
		return textDms;
	}
	
	public DoubleMatrix RepresentText(DoubleMatrix[] textEmbs)
	{
		if(textEmbs==null||textEmbs.length==0)
		{
			return new DoubleMatrix(1,gru.getOutSize());
		}
		
		DoubleMatrix[] xs=textEmbs;
		DoubleMatrix[] hs=new DoubleMatrix[textEmbs.length];
		DoubleMatrix[] rs=new DoubleMatrix[textEmbs.length];
		DoubleMatrix[] zs=new DoubleMatrix[textEmbs.length]; 
		DoubleMatrix[] ghs=new DoubleMatrix[textEmbs.length];
		
		for(int i=0;i<textEmbs.length;i++)
		{
			gru.active(xs,hs,rs,zs,ghs,i);
		}
		return gru.LastY(hs[textEmbs.length-1]);
	}
}
