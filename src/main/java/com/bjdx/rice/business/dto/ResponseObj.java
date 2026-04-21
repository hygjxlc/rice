/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.bjdx.rice.business.dto;

import com.bjdx.rice.business.exception.ResponseCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 返回数据
 */
@Data
@ApiModel
public class ResponseObj<T>  {
	@JsonProperty("code")
	@ApiModelProperty("响应码")
	private Integer code;
	@JsonProperty("message")
    @ApiModelProperty("响应信息")
	private String message;
	@JsonProperty("data")
    @ApiModelProperty("响应数据")
	private T data;

	public ResponseObj() {

	}

	public ResponseObj put(T data) {
		this.data=data;
		return this;
	}
	@JsonCreator
	public ResponseObj(
			@JsonProperty("code") Integer code,
			@JsonProperty("message") String message,
			@JsonProperty("data") T data
	) {
		this.code = code;
		this.message = message;
		this.data = data;
	}


	public void setData(Object data){
	    this.data = (T) data;
    }

	public static ResponseObj error() {
		ResponseObj jsonResponse = new ResponseObj();
		jsonResponse.setCode(ResponseCode.FAIL.code);
		jsonResponse.setMessage(ResponseCode.FAIL.msg);

		return jsonResponse;
	}

    public static ResponseObj error(String message) {
        ResponseObj jsonResponse = new ResponseObj();
        jsonResponse.setCode(ResponseCode.FAIL.code);
        jsonResponse.setMessage(message);

        return jsonResponse;
    }

	public static ResponseObj error(ResponseCode responseCode) {
		ResponseObj jsonResponse = new ResponseObj();
		jsonResponse.setCode(responseCode.code);
		jsonResponse.setMessage(responseCode.msg);

		return jsonResponse;
	}

	public static ResponseObj error(int code,String message) {
		ResponseObj jsonResponse = new ResponseObj();
		jsonResponse.setCode(code);
		jsonResponse.setMessage(message);

		return jsonResponse;
	}

	public static ResponseObj warn(String message) {
		ResponseObj jsonResponse = new ResponseObj();
		jsonResponse.setCode(ResponseCode.FAIL.code);
		jsonResponse.setMessage(message);
		jsonResponse.setData(null);
		return jsonResponse;
	}

    public static ResponseObj success() {
        ResponseObj jsonResponse = new ResponseObj();
        jsonResponse.setCode(ResponseCode.SUCCECC.code);
        jsonResponse.setMessage("操作成功");
        return jsonResponse;
    }

	public static ResponseObj success(Object data) {
		ResponseObj jsonResponse = new ResponseObj();
		jsonResponse.setCode(ResponseCode.SUCCECC.code);
		jsonResponse.setData(data);
		jsonResponse.setMessage("操作成功");
		return jsonResponse;
	}
}
