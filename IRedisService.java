package com.foresee.mobile.api.service.biz;

import java.io.Serializable;

/**
 * redis 服务
 *
 */
public interface IRedisService {

	/**
	 * 把字符串插入到redis服务器，默认超时时间为1小时
	 * @param key
	 * @param value
	 */
	public void setString(String key, String value);

	/**
	 * 把字符串插入到redis服务器，指定超时时间，单位（秒）
	 * @param key
	 * @param value
	 * @param expiredSeconds
	 */
	public void setString(String key, String value, int expiredSeconds);

	/**
	 * 把对象插入到redis服务器，默认超时时间为1小时
	 * @param key
	 * @param value
	 */
	public void setObject(String key, Serializable value);

	/**
	 * 把对象插入到redis服务器，指定超时时间，单位（秒）
	 * @param key
	 * @param value
	 * @param expiredSeconds
	 */
	public void setObject(String key, Serializable value, int expiredSeconds);

	/**
	 * 从redis中获取字符串
	 * @param key
	 * @return
	 */
	public String getString(String key);

	/**
	 * 从redis中获取字符串，并重新设置超时时间，单位（秒）
	 * @param key
	 * @param expiredSeconds
	 * @return
	 */
	public String getString(String key, int expiredSeconds);

	/**
	 * 从redis中获取对象
	 * @param key
	 * @return
	 */
	public Object getObject(String key);

	/**
	 * 从redis中获取对象，并重新设置超时时间，单位（秒）
	 * @param key
	 * @param expiredSeconds
	 * @return
	 */
	public Object getObject(String key, int expiredSeconds);

	/**
	 * 从redis中删除指定key
	 * @param key
	 */
	public Long removeObject(String key);

	/**
	 * 设置redis中指定key的超时时间，单位（秒）
	 * @param key
	 * @param expiredSeconds
	 */
	public void expire(String key, int expiredSeconds);

	/**
	 * 获取redis中所有数据的数量
	 * @return
	 */
	public long getRedisSize();
	
	/**
	 * 判断指定key值是否存在，存在则返回false，不存在则增加key，同时返回true
	 * @param key
	 * @return
	 */
	public boolean isUnLock(String key);
	
	/**
	 * 获取指定key值的有效时间
	 * @param key
	 * @return
	 */
	public Long ttl(String key);
	
	/**
	 * 判断指定key值是否存在，存在则返回false，不存在则增加key，同时返回true，可以指定key的存活时间
	 * @param key
	 * @param expiredSeconds
	 * @return
	 */
	public boolean isUnLock(String key, int expiredSeconds);

	public String lock(String key);

	public String lock(String key, int lockTime);

	public boolean unlock(String key, String lock);

	public Long incr(String key);

	public Long incrBy(String key, long value);

	public Long decr(String key);

	public Long decrBy(String key, long value);
}
