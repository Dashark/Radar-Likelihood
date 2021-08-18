package com.powervotex.localserver.algorithm.service;

import java.util.Map;
import java.util.Set;

import com.powervotex.localserver.algorithm.dto.CoObject;

/**
 * 目标（Target）与检测对象（Object）的关联概率服务
 * 计算 T - O 的关联概率
 */
public interface ITargetObjectService {

	/**
	 * 更新场景中相关对象的清单
	 * 更新针对于整个场景，不是一部分。
	 * 
	 * @param targets 实际的业务目标
	 * @param radars  雷达检测的对象
	 * @return 更新对象的数量
	 */
	public int updateObjects(Set<CoObject> targets, Set<CoObject> radars);

	/**
	 * 通过RFID技术手段绑定业务目标与雷达检测对象
	 * 它们应该存在于对象的清单中。
	 * 
	 * @param target
	 * @param radar
	 * @return
	 */
	public boolean matchByRFID(CoObject target, CoObject radar);
	
	/**
	 * 计算目标的关联概率。
	 * 
	 * @param Object target 目标
	 * @return float 概率
	 */
	public double[] calculateProbability();

	/**
	 * 查询概率表
	 * @return Map<target id, Map<radar id, probability>>
	 */
	public Map<String, Map<String, Double>> queryProbability();

}
