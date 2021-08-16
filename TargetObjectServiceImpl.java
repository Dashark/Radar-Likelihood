package com.powervotex.localserver.radar.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.powervotex.localserver.business.common.utils.RedisUtil;
import com.powervotex.localserver.radar.service.CoObject;
import com.powervotex.localserver.radar.service.ICorrelationProbabilityService;
import com.powervotex.localserver.radar.service.ITargetObjectService;
import com.powervotex.localserver.radar.service.RadarObject;

@Service
public class TargetObjectServiceImpl implements ITargetObjectService {

	private ICorrelationProbabilityService _cp_service; 
	private Map<CoObject, Integer> _business_targets = Maps.newHashMap(); 
	private Set<CoObject> _new_targets = new HashSet<CoObject>();
	private Set<CoObject> _del_targets = new HashSet<CoObject>();
	private Map<CoObject, Integer> _radar_objects = Maps.newHashMap(); 
	private Set<CoObject> _new_objects = new HashSet<CoObject>();
	private Set<CoObject> _del_objects = new HashSet<CoObject>();
	private double[] _probabilities;
	public TargetObjectServiceImpl() {
		
	}

	private Set<CoObject> diffSet(Set<CoObject> orig, Set<CoObject> newSet) {
		return Set;
	} 
	/**
	 * 清除 probabilities 中消失的目标和检测对象的概率
	 * @param targets
	 * @param radars
	 */
	private double[] removeProbs(Integer[] targets, Integer[] radars) {
		
	}

	private Integer[] mapIndex(Map<CoObject, Integer> maps, Set<CoObject> objs) {

	}
	@Override
	public int updateObjects(Set<CoObject> targets, Set<CoObject> radars) {
		// 新的概率映射表
		double[] probs_new = new double[targets.size() * radars.size()];
		Map<CoObject, Integer> business_targets = Maps.newHashMap(); 
		Map<CoObject, Integer> radar_objects = Maps.newHashMap(); 
		Integer tar_idx = 0;  // 新表的目标索引（行）
		for (CoObject tar : targets) {
			Integer obj_idx = 0;   // 新表的检测对象索引(列)
			Integer tidx = _business_targets.get(tar); // 原有目标集合的索引值
			if (tidx == null) {
				// 新增的目标
			  for (CoObject obj : radars) {
				  Integer oidx = _radar_objects.get(obj); // 原有检测对象集合的索引值
				  if (oidx == null) {
					  // 新增的检测对象，对应新增的目标，概率设置较高
						probs_new[tar_idx * targets.size() + obj_idx] = 0.5;
				  }
				  else {
					  // 原有的检测对象，对应新增的目标，概率设置较低
						probs_new[tar_idx * targets.size() + obj_idx] = 0.05;
				  }
					radar_objects.put(obj, obj_idx);
					obj_idx += 1;  // 下一个检测对象
			  }
			}
			else {
				// 原有的目标，不管原来的Idx
			  for (CoObject obj : radars) {
				  Integer oidx = _radar_objects.get(obj);
				  if (oidx == null) {
				  	// 新增的检测对象，对应原有的目标，概率设置很低
						probs_new[tar_idx * targets.size() + obj_idx] = 0.05;
				  }
				  else {
				  	// 原有的检测对象，对应原有的目标，拷贝上次计算的概率
						probs_new[tar_idx * targets.size() + obj_idx] = _probabilities[tidx * _business_targets.size() + oidx];
				  }
					radar_objects.put(obj, obj_idx);
					obj_idx += 1;  // 下一个检测对象
			  }
			}
			business_targets.put(tar, tar_idx);
			tar_idx += 1; // 下一个目标
		}
		_probabilities = probs_new;  // 刷新概率表
		_business_targets = business_targets;
		_radar_objects = radar_objects;
		return targets.size() + radars.size();
	}

	@Override
	public boolean matchByRFID(CoObject target, CoObject radar) {
		Integer tidx = _business_targets.get(target);
		Integer oidx = _radar_objects.get(radar);
		if (tidx != null && oidx != null) {
			for (int i = 0; i < _radar_objects.size(); ++i) {
				// 原有概率全部清零
				_probabilities[tidx * _business_targets.size() + i] = 0.0;
			}
			_probabilities[tidx * _business_targets.size() + oidx] = 1.0; // 确定是匹配的
			target.setEigen(radar);
			return true;
		}
		return false;
	}

	@Override
	public double[] queryProbability() {
		// 特征向量
		double[] tarobjs = new double[3 * _business_targets.size()];
		// 条件概率
		double[] cond = new double[_business_targets.size()];
		for (CoObject obj : _radar_objects.keySet()) {
			// 一个检测对象
			Integer obj_idx = _radar_objects.get(obj);
			// 映射所有的目标
			for (CoObject tar : _business_targets.keySet()) {
				Integer tar_idx = _business_targets.get(tar);
				cond[tar_idx] = _probabilities[tar_idx * _business_targets.size() + obj_idx];
				double[] temps = tar.subEigen(obj);
				tarobjs[tar_idx] = temps[0];
				tarobjs[tar_idx + _business_targets.size()] = temps[1];
				tarobjs[tar_idx + _business_targets.size() * 2] = temps[2];
			}
			double[] prob_obj = _cp_service.calcuateProbability(cond, tarobj);
			for (CoObject tar : _business_targets.keySet()) {
				Integer tar_idx = _business_targets.get(tar);
				_probabilities[tar_idx * _business_targets.size() + obj_idx] = prob_obj[tar_idx];
		}

		return _probabilities;
	}
}
