package com.powervotex.localserver.algorithm.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.powervotex.localserver.algorithm.dto.CoObject;
import com.powervotex.localserver.algorithm.service.ICorrelationProbabilityService;
import com.powervotex.localserver.algorithm.service.ITargetObjectService;

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

	/*
	public class CoResult {
		private CoObject target, radar;
		// public int target_idx, radar_idx;
		private float probability;
		public CoResult(CoObject target, CoObject radar) {
			this.target = target;
			this.radar = radar;
			probability = 0.0f;
		}

		public float modifyProb(float prob) {
			float ret = probability;
			probability = prob;
			return ret;
		}

		public float modifyProb(CoResult other) {
			float ret = probability;
			probability = other.probability;
			return ret;
		}

		public boolean equalTarget(CoResult other) {
			if (target.equals(other.target))
				return true;
			return false;
		}
		
		public boolean equalRadar(CoResult other) {
			if (radar.equals(other.radar))
				return true;
			return false;
		}

		@Override
		public int hashCode() {
			String tmp = target.getID() + radar.getID();
			return tmp.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CoResult) {
				CoResult cores = (CoResult)obj;
				return target.equals(cores.target) && radar.equals(cores.radar);
			}
			return false;
		}
	}
	*/

	/*
	@Override
	public int updateObjects(Set<CoObject> targets, Set<CoObject> radars) {
		Set<CoResult> corSet = new HashSet<CoResult>();
		for (CoObject tar : targets) {
			for (CoObject obj : radars) {
				CoResult cor = new CoResult(tar, obj); // 新的概率表，0.0
				corSet.add(cor);
			}
		}
		// 2个List表的数据合并
		// 原有目标和原有检测对象复制，消失的则丢弃了
		for (CoResult newCO : corSet) {
			for (CoResult origCO : prob_map) {
				boolean radar_eq = newCO.equalRadar(origCO) ;
				boolean target_eq = newCO.equalTarget(origCO);
				// 原有的目标和原有的检测对象
				if (newCO.equals(origCO)) {
					newCO.modifyProb(origCO);
				}
			}
		}
		// 新增目标和新增检测对象的概率设置，它们概率应该较高
		// 新增目标和原有检测对象，原有目标和新增检测对象，它们概率应该很低
	}
	*/
	@Override
	public int updateObjects(Set<CoObject> targets, Set<CoObject> radars) {
		// 新的概率映射表
		double[] probs_new = new double[targets.size() * radars.size()];
		Map<CoObject, Integer> business_targets = Maps.newHashMap();
		Map<CoObject, Integer> radar_objects = Maps.newHashMap();
		Integer tar_idx = 0; // 新表的目标索引（行）
		for (CoObject tar : targets) {
			Integer obj_idx = 0; // 新表的检测对象索引(列)
			Integer tidx = _business_targets.get(tar); // 原有目标集合的索引值
			if (tidx == null) {
				// 新增的目标
				for (CoObject obj : radars) {
					Integer oidx = _radar_objects.get(obj); // 原有检测对象集合的索引值
					if (oidx == null) {
						// 新增的检测对象，对应新增的目标，概率设置较高
						probs_new[tar_idx * targets.size() + obj_idx] = 0.5;
					} else {
						// 原有的检测对象，对应新增的目标，概率设置较低
						probs_new[tar_idx * targets.size() + obj_idx] = 0.05;
					}
					radar_objects.put(obj, obj_idx);
					obj_idx += 1; // 下一个检测对象
				}
			} else {
				// 原有的目标，不管原来的Idx
				for (CoObject obj : radars) {
					Integer oidx = _radar_objects.get(obj);
					if (oidx == null) {
						// 新增的检测对象，对应原有的目标，概率设置很低
						probs_new[tar_idx * targets.size() + obj_idx] = 0.05;
					} else {
						// 原有的检测对象，对应原有的目标，拷贝上次计算的概率
						probs_new[tar_idx * targets.size() + obj_idx] = _probabilities[tidx * _business_targets.size()
								+ oidx];
					}
					radar_objects.put(obj, obj_idx);
					obj_idx += 1; // 下一个检测对象
				}
			}
			business_targets.put(tar, tar_idx);
			tar_idx += 1; // 下一个目标
		}
		_probabilities = probs_new; // 替换概率表，此时为条件概率
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
	public double[] calculateProbability() {
		// 特征向量 3 x n
		double[] tarobjs = new double[3 * _business_targets.size()];
		// 条件概率 1 x n
		double[] cond = new double[_business_targets.size()];
		for (CoObject obj : _radar_objects.keySet()) {
			// 一个检测对象
			Integer obj_idx = _radar_objects.get(obj);
			// 映射所有的目标
			for (CoObject tar : _business_targets.keySet()) {
				Integer tar_idx = _business_targets.get(tar);
				// 上一轮概率作为条件概率
				cond[tar_idx] = _probabilities[tar_idx * _business_targets.size() + obj_idx];
				double[] temps = tar.subEigen(obj);
				// 行转换列
				tarobjs[tar_idx] = temps[0];
				tarobjs[tar_idx + _business_targets.size()] = temps[1];
				tarobjs[tar_idx + _business_targets.size() * 2] = temps[2];
			}
			// 计算新的概率（一个检测对象与所有目标的）
			double[] prob_obj = _cp_service.calcuateProbability(cond, tarobjs);
			// 更新概率表, 按照检测对象更新所有目标
			for (CoObject tar : _business_targets.keySet()) {
				Integer tar_idx = _business_targets.get(tar);
				_probabilities[tar_idx * _business_targets.size() + obj_idx] = prob_obj[tar_idx];
			}

		}
		return _probabilities;
	}
	@Override
	public Map<String, Map<String, Float>> queryProbability() {
		Map<String, Map<String, Float>> results = Maps.newHashMap();
		for (CoObject tar : _business_targets.keySet()) {
			// 一个目标
			Integer tar_idx = _business_targets.get(tar);
			Map<String, Float> radar_result = Maps.newHashMap();
			for (CoObject obj : _radar_objects.keySet()) {
				// 一个检测对象
				Integer obj_idx = _radar_objects.get(obj);
				radar_result.put(obj.getID(), _probabilities[tar_idx * _business_targets.size() + obj_idx]);
			}
			results.put(tar.getID(), radar_result);
		}
		return results;
	}
}
