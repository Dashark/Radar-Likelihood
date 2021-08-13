package com.powervotex.localserver.radar.service;

import java.io.Serializable;

import lombok.Data;

@Data
public class RadarObject implements Serializable {

	private static final long serialVersionUID = -54421651138265510L;

	/**
	 * 雷达检测对象的ID
	 */
	private String id;
	
	/**
	 * 雷达检测对象的坐标（X，Y，Z）
	 */
	private Integer X, Y, Z;

	/**
	 * 特征值
	 */
	private double[] eigen;
}
