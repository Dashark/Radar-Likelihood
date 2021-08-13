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
import com.powervotex.localserver.radar.service.ITargetObjectService;

@Service
public class TargetObjectServiceImpl implements ITargetObjectService {

	private final ICorrelationProbabilityService _cp_service; 
	private final Set<BusinessTarget> _business_targets = new HashSet<BusinessTarget>();
	private final Set<RadarTarget> _radar_objects = new HashSet<RadarTarget>();
	private TargetObjectServiceImpl() {
		
	}

	@Override
	public int updateRadarObjects(Set<RadarObject> objs) {
		_radar_objects = objs;
		return objs.size();
	}

	@Override
	public boolean matchByRFID(BusinessTarget btar, RadarTarget rtar) {
		_business_targets.addObject(btar);
		_radar_objects.addObject(rtar);
		return false;
	}

	@Override
	public Map<RadarTarget, Decimal> queryProbability(BusinessTarget target) {

		return null;
	}
}
