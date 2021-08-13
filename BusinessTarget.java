package com.powervotex.localserver.radar.service;

import java.io.Serializable;

import lombok.Data;

@Data
public class BusinessTarget implements Serializable {

	private static final long serialVersionUID = -5154421651138265510L;

	/**
	 * 业务目标的ID
	 */
	private String id;
	
	/**
	 * 目标的三维坐标
	 */
	private Integer X, Y, Z;
	
	/**
	 * 特征值
	 */
	private double[] eigen;

	/**
	 * 其它信息
	 */
	private String info;
}
