package com.boraydata.flowauth.utils

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.StringUtils

/**
 * json工具类
 */
object JsonUtils {

  /**
   * 将json字符串转换为泛型T的对象
   */
  fun <T> parse(json: String, clazz: Class<T>): T {
    return when (StringUtils.isNotEmpty(json)) {
      true -> {
        JSONObject.parseObject(json, clazz)
      }
      false -> clazz.newInstance()
    }
  }

  /**
   * 将json数组字符串转换为泛型T的对象集合
   */
  fun <T> parseArr(json: String, clazz: Class<T>): List<T> {
    return when (StringUtils.isNotEmpty(json)) {
      true -> {
        JSONArray.parseArray(json).map { JSONObject.parseObject(it.toString(), clazz) }
      }
      false -> listOf()
    }
  }

  /**
   * 将实体类转换为字符串
   */
  fun <T> toJsonStr(obj: T): String {
    return JSONObject.toJSONString(obj)
  }
}
