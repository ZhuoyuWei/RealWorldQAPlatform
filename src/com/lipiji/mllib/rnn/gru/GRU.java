package com.lipiji.mllib.rnn.gru;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.wzy.method.trImpl.GRURepresentation;

import com.lipiji.mllib.layers.MatIniter;
import com.lipiji.mllib.layers.MatIniter.Type;
import com.lipiji.mllib.utils.Activer;

public class GRU implements Serializable {
    private static final long serialVersionUID = -1501734916541393551L;

    private int inSize;
    private int outSize;
    private int deSize;
    
    private DoubleMatrix Wxr;
    private DoubleMatrix Whr;
    private DoubleMatrix br;
    
    private DoubleMatrix Wxz;
    private DoubleMatrix Whz;
    private DoubleMatrix bz;
    
    private DoubleMatrix Wxh;
    private DoubleMatrix Whh;
    private DoubleMatrix bh;
    
    private DoubleMatrix Why;
    private DoubleMatrix by;
    
    DoubleMatrix gWxr;// = new DoubleMatrix(Wxr.rows, Wxr.columns);
    DoubleMatrix gWhr;// = new DoubleMatrix(Whr.rows, Whr.columns);
    DoubleMatrix gbr;// = new DoubleMatrix(br.rows, br.columns);
    
    DoubleMatrix gWxz;// = new DoubleMatrix(Wxz.rows, Wxz.columns);
    DoubleMatrix gWhz;// = new DoubleMatrix(Whz.rows, Whz.columns);
    DoubleMatrix gbz;// = new DoubleMatrix(bz.rows, bz.columns);
    
    DoubleMatrix gWxh;// = new DoubleMatrix(Wxh.rows, Wxh.columns);
    DoubleMatrix gWhh;// = new DoubleMatrix(Whh.rows, Whh.columns);
    DoubleMatrix gbh;// = new DoubleMatrix(bh.rows, bh.columns);
    
    DoubleMatrix gWhy;// = new DoubleMatrix(Why.rows, Why.columns);
    DoubleMatrix gby;// = new DoubleMatrix(by.rows, by.columns);
    
    public List<double[][]> gwords;
    public List<double[][]> swords;
    
    public void InitGradients()
    {
        gWxr = new DoubleMatrix(Wxr.rows, Wxr.columns);
        gWhr = new DoubleMatrix(Whr.rows, Whr.columns);
        gbr = new DoubleMatrix(br.rows, br.columns);
        
        gWxz = new DoubleMatrix(Wxz.rows, Wxz.columns);
        gWhz = new DoubleMatrix(Whz.rows, Whz.columns);
        gbz = new DoubleMatrix(bz.rows, bz.columns);
        
        gWxh = new DoubleMatrix(Wxh.rows, Wxh.columns);
        gWhh = new DoubleMatrix(Whh.rows, Whh.columns);
        gbh = new DoubleMatrix(bh.rows, bh.columns);
        
        gWhy = new DoubleMatrix(Why.rows, Why.columns);
        gby = new DoubleMatrix(by.rows, by.columns);
    }
    
    
    public GRU(int inSize, int outSize, MatIniter initer) {
        this.inSize = inSize;
        this.outSize = outSize;
        
        if (initer.getType() == Type.Uniform) {
            this.Wxr = initer.uniform(inSize, outSize);
            this.Whr = initer.uniform(outSize, outSize);
            this.br = new DoubleMatrix(1, outSize);
            
            this.Wxz = initer.uniform(inSize, outSize);
            this.Whz = initer.uniform(outSize, outSize);
            this.bz = new DoubleMatrix(1, outSize);
            
            this.Wxh = initer.uniform(inSize, outSize);
            this.Whh = initer.uniform(outSize, outSize);
            this.bh = new DoubleMatrix(1, outSize);
            
            this.Why = initer.uniform(outSize, inSize);
            this.by = new DoubleMatrix(1, inSize);
        } else if (initer.getType() == Type.Gaussian) {
        }
    }
    
    public GRU(int inSize, int outSize, MatIniter initer, int deSize) {
        this(inSize, outSize, initer);
        this.deSize = deSize;
        this.Why = new DoubleMatrix(outSize, deSize);
        this.by = new DoubleMatrix(1, deSize);
    }
    
    public int getInSize() {
        return inSize;
    }

    public int getOutSize() {
        return outSize;
    }
    
    public int getDeSize() {
        return deSize;
    }
    
    /*public void active(int t, Map<String, DoubleMatrix> acts) {
        DoubleMatrix x = acts.get("x" + t);
        DoubleMatrix preH = null;
        if (t == 0) {
            preH = new DoubleMatrix(1, getOutSize());
        } else {
            preH = acts.get("h" + (t - 1));
        }
        
        DoubleMatrix r = Activer.logistic(x.mmul(Wxr).add(preH.mmul(Whr)).add(br));
        DoubleMatrix z = Activer.logistic(x.mmul(Wxz).add(preH.mmul(Whz)).add(bz));
        DoubleMatrix gh = Activer.tanh(x.mmul(Wxh).add(r.mul(preH).mmul(Whh)).add(bh));
        DoubleMatrix h = (DoubleMatrix.ones(1, z.columns).sub(z)).mul(preH).add(z.mul(gh));
        
        acts.put("r" + t, r);
        acts.put("z" + t, z);
        acts.put("gh" + t, gh);
        acts.put("h" + t, h);
    }*/
    
    public void active(DoubleMatrix[] xs,
    				   DoubleMatrix[] hs,
    				   DoubleMatrix[] rs,
    				   DoubleMatrix[] zs,   
    				   DoubleMatrix[] ghs, 
    				   int t) {
        DoubleMatrix x = xs[t];
        DoubleMatrix preH = null;
        if (t == 0) {
            preH = new DoubleMatrix(1, getOutSize());
        } else {
            preH = hs[t-1];
        }
        
      //  System.out.println("\tbr "+br);
        
        DoubleMatrix r = Activer.logistic(x.mmul(Wxr).add(preH.mmul(Whr)).add(br));
        DoubleMatrix z = Activer.logistic(x.mmul(Wxz).add(preH.mmul(Whz)).add(bz));
        DoubleMatrix gh = Activer.tanh(x.mmul(Wxh).add(r.mul(preH).mmul(Whh)).add(bh));
        DoubleMatrix h = (DoubleMatrix.ones(1, z.columns).sub(z)).mul(preH).add(z.mul(gh));
        
        /*acts.put("r" + t, r);
        acts.put("z" + t, z);
        acts.put("gh" + t, gh);
        acts.put("h" + t, h);*/
      //  System.out.println(rs+"\t"+zs+"\t"+ghs+"\t"+hs);
        rs[t]=r;
        zs[t]=z;
        ghs[t]=gh;
        hs[t]=h;
    }    
    
    
    public void bptt(DoubleMatrix[] xs,
			   DoubleMatrix[] hs,
			   DoubleMatrix[] rs,
			   DoubleMatrix[] zs,   
			   DoubleMatrix[] ghs,
			   int lastT, DoubleMatrix lost)
    {
    	//DoubleMatrix[] dxs;
    	DoubleMatrix[] dhs=new DoubleMatrix[hs.length];
		DoubleMatrix[] drs=new DoubleMatrix[rs.length];
		DoubleMatrix[] dzs=new DoubleMatrix[zs.length];   
		DoubleMatrix[] dghs=new DoubleMatrix[ghs.length];
    	
    	
        for (int t = lastT; t > -1; t--) {
            /*DoubleMatrix py = acts.get("py" + t);
            DoubleMatrix y = acts.get("y" + t);
            DoubleMatrix deltaY = py.sub(y);
            acts.put("dy" + t, deltaY);
            */
        	
        	DoubleMatrix deltaY = lost;
        	
            // cell output errors
            DoubleMatrix h = hs[t];
            DoubleMatrix z =zs[t];
            DoubleMatrix r = rs[t];
            DoubleMatrix gh = ghs[t];
            
            DoubleMatrix deltaH = null;
            if (t == lastT) {
                deltaH = Why.mmul(deltaY.transpose()).transpose();
            } else {
                DoubleMatrix lateDh = dhs[t+1];
                DoubleMatrix lateDgh = dghs[t+1];
                DoubleMatrix lateDr = drs[t+1];
                DoubleMatrix lateDz = dzs[t+1];
                DoubleMatrix lateR = rs[t+1];
                DoubleMatrix lateZ = zs[t+1];
                //deltaH = Why.mmul(deltaY.transpose()).transpose().add
                deltaH =(Whr.mmul(lateDr.transpose()).transpose())
                        .add(Whz.mmul(lateDz.transpose()).transpose())
                        .add(Whh.mmul(lateDgh.mul(lateR).transpose()).transpose())
                        .add(lateDh.mul(DoubleMatrix.ones(1, lateZ.columns).sub(lateZ)));
            }
            //acts.put("dh" + t, deltaH);
            dhs[t]=deltaH;
            
            // gh
            DoubleMatrix deltaGh = deltaH.mul(z).mul(deriveTanh(gh));
            //acts.put("dgh" + t, deltaGh);
            dghs[t]=deltaGh;
            
            DoubleMatrix preH = null;
            if (t > 0) {
                //preH = acts.get("h" + (t - 1));
            	preH=hs[t-1];
            } else {
                preH = DoubleMatrix.zeros(1, h.length);
            }
            
            // reset gates
            DoubleMatrix deltaR = (Whh.mmul(deltaGh.mul(preH).transpose()).transpose()).mul(deriveExp(r));
            //acts.put("dr" + t, deltaR);
            drs[t]=deltaR;
            
            // update gates
            DoubleMatrix deltaZ = deltaH.mul(gh.sub(preH)).mul(deriveExp(z));
            //acts.put("dz" + t, deltaZ); 
            dzs[t]=deltaZ;
        }
        CalculateParameterGradients(xs,hs,rs,
        				 dhs,drs,dzs,dghs,
        				 lastT,lost);
    }
    
    private void CalculateParameterGradients(DoubleMatrix[] xs,
			   					  DoubleMatrix[] hs,
			   					  DoubleMatrix[] rs,
			   					  //DoubleMatrix[] zs,   
			   					  //DoubleMatrix[] ghs,  		
    							  DoubleMatrix[] dhs,
    							  DoubleMatrix[] drs,
    							  DoubleMatrix[] dzs,  
    							  DoubleMatrix[] dghs,
    							  int lastT,DoubleMatrix lost) 
    {        
    	
    	 DoubleMatrix[] gx=null;
         
         if(GRURepresentation.update_wordembs)
         {
         	gx=new DoubleMatrix[xs.length];
         	for(int i=0;i<=lastT;i++)
         	{
         		gx[i]=new DoubleMatrix(xs[i].rows,xs[i].columns);
         	}
         }
    	
        for (int t = 0; t < lastT + 1; t++) {
            DoubleMatrix x = xs[t].transpose();
            gWxr = gWxr.add(x.mmul(drs[t]));
            gWxz = gWxz.add(x.mmul(dzs[t]));
            gWxh = gWxh.add(x.mmul(dghs[t]));
            
            if(GRURepresentation.update_wordembs)
            {
            	gx[t]=gx[t].add(drs[t].mmul(gWxr));
            	gx[t]=gx[t].add(dzs[t].mmul(gWxz));
            	gx[t]=gx[t].add(dghs[t].mmul(gWxh));
            }
            
            if (t > 0) {
                DoubleMatrix preH = hs[t-1].transpose();
                gWhr = gWhr.add(preH.mmul(drs[t]));
                gWhz = gWhz.add(preH.mmul(dzs[t]));
                gWhh = gWhh.add(rs[t].transpose().mul(preH).mmul(dghs[t]));
            }
            if(t==lastT)
            {
            	gWhy = gWhy.add(hs[t].transpose().mmul(lost));
            }
            
            gbr = gbr.add(drs[t]);
            gbz = gbz.add(dzs[t]);
            gbh = gbh.add(dghs[t]);
            if(t==lastT)
            {            
            	gby = gby.add(lost);
            }
        }
        
        /*Wxr = Wxr.sub(clip(gWxr.div(lastT)).mul(lr));
        Whr = Whr.sub(clip(gWhr.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        br = br.sub(clip(gbr.div(lastT)).mul(lr));
        
        Wxz = Wxz.sub(clip(gWxz.div(lastT)).mul(lr));
        Whz = Whz.sub(clip(gWhz.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        bz = bz.sub(clip(gbz.div(lastT)).mul(lr));
        
        Wxh = Wxh.sub(clip(gWxh.div(lastT)).mul(lr));
        Whh = Whh.sub(clip(gWhh.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        bh = bh.sub(clip(gbh.div(lastT)).mul(lr));
        
        Why = Why.sub(clip(gWhy.div(lastT)).mul(lr));
        by = by.sub(clip(gby.div(lastT)).mul(lr));*/
        
        if(GRURepresentation.update_wordembs)
        {
        	
        	double[][] gx_double=new double[gx.length][];
        	for(int i=0;i<gx.length;i++)
        	{
        		gx_double[i]=gx[i].toArray();
        	}
        	gwords.add(gx_double);
        	//swords.add(words);
        }
        
    }
    
    public void UpdateParameters(double lr)
    {
    	Wxr = Wxr.sub(clip(gWxr).mul(lr));
        Whr = Whr.sub(clip(gWhr).mul(lr));
        br = br.sub(clip(gbr).mul(lr));
        
        Wxz = Wxz.sub(clip(gWxz).mul(lr));
        Whz = Whz.sub(clip(gWhz).mul(lr));
        bz = bz.sub(clip(gbz).mul(lr));
        
        Wxh = Wxh.sub(clip(gWxh).mul(lr));
        Whh = Whh.sub(clip(gWhh).mul(lr));
        bh = bh.sub(clip(gbh).mul(lr));
        
        Why = Why.sub(clip(gWhy).mul(lr));
        by = by.sub(clip(gby).mul(lr));
        
        if(GRURepresentation.update_wordembs)
        {
        	if(swords!=null&&gwords!=null&&swords.size()==gwords.size())
        	{
        		for(int i=0;i<swords.size();i++)
        		{
        			double[][] xs=swords.get(i);
        			double[][] gxs=gwords.get(i);
        			
        			if(xs.length==gxs.length)
        			{
        				for(int j=0;j<xs.length;j++)
        				{
        					//xs[j]=xs[j].sub(clip(gxs[j]).mul(lr));
        					for(int k=0;k<xs[j].length;k++)
        					{
        						//clip?
        						
        						xs[j][k]-=gxs[j][k]*lr;
        					}
        				}
        			}
        		}
        	}
        }
        
    }
    
    private DoubleMatrix deriveExp(DoubleMatrix f) {
        return f.mul(DoubleMatrix.ones(1, f.length).sub(f));
    }
    
    private DoubleMatrix deriveTanh(DoubleMatrix f) {
        return DoubleMatrix.ones(1, f.length).sub(MatrixFunctions.pow(f, 2));
    }
    
    private DoubleMatrix clip(DoubleMatrix x) {
        //double v = 10;
        //return x.mul(x.ge(-v).mul(x.le(v)));
        return x;
    }
    
    public DoubleMatrix decode (DoubleMatrix ht) {
        return Activer.softmax(ht.mmul(Why).add(by));
    }
    public DoubleMatrix LastY(DoubleMatrix ht)
    {
    	return ht.mmul(Why).add(by);
    }
}
