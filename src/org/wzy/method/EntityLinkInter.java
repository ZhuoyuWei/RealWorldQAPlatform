package org.wzy.method;

import java.util.List;
import java.util.Map;

import org.wzy.meta.NELink;

public interface EntityLinkInter {

	public List<NELink> LinkingString(String text);
	public void SetEntityAndRelationMap(Map<String,Integer> entity2id,Map<String,Integer> relation2id);
}
