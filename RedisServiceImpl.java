package com.foresee.mobile.base.common.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.foresee.mobile.api.constants.RedisConstants;
import com.foresee.mobile.api.service.biz.IRedisService;
import com.foresee.mobile.api.utils.ObjectUtil;

/**
 * 
 *
 */
@SuppressWarnings({ "unchecked" })
//@Service("redisService")
public class RedisServiceImpl implements IRedisService {

	@Resource(name = "redisTemplate")
	private RedisTemplate<?, ?> redisTemplate;

	@Resource(name = "lockScript")
	private RedisScript<Long> lockScript;

	@Resource(name = "unlockScript")
	private RedisScript<Long> unlockScript;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foresee.base.redis.IRedisService#setString(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setString(String key, String value) {
		setString(key, value, RedisConstants.TTL_HALF_HOUR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foresee.base.redis.IRedisService#setString(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public void setString(String key, String value, final int expiredSeconds) {
		final String keyf = (String) key;
		final String valuef = value;

		redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] keyb = keyf.getBytes();
				byte[] valueb = valuef.getBytes();
				connection.set(keyb, valueb);
				if (expiredSeconds > 0) {
					connection.expire(keyb, expiredSeconds);
				}
				return 1L;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foresee.base.redis.IRedisService#setObject(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void setObject(String key, Serializable value) {
		setObject(key, value, RedisConstants.TTL_HALF_HOUR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foresee.base.redis.IRedisService#setObject(java.lang.String,
	 * java.lang.Object, int)
	 */
	@Override
	public void setObject(String key, Serializable value, final int expiredSeconds) {
		final String keyf = (String) key;
		final Object valuef = value;

		redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] keyb = keyf.getBytes();
				byte[] valueb = ObjectUtil.toByteArray(valuef);
				connection.set(keyb, valueb);
				if (expiredSeconds > 0) {
					connection.expire(keyb, expiredSeconds);
				}
				return 1L;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foresee.base.redis.IRedisService#getString(java.lang.String)
	 */
	@Override
	public String getString(String key) {
		return this.getString(key, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foresee.base.redis.IRedisService#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String key) {
		return this.getObject(key, 0);
	}

	public void evict(Object key) {
		final String keyf = (String) key;
		redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.del(keyf.getBytes());
			}
		});
	}

	public void clear() {
		redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushDb();
				return "ok";
			}
		});
	}

	@Override
	public String getString(String key, final int expiredSeconds) {
		final String keyf = (String) key;
		String object = null;
		object = (String) redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] key = keyf.getBytes();
				byte[] value = connection.get(key);
				if (value == null) {
					return null;
				}
				if (expiredSeconds > 0) {
					connection.expire(key, expiredSeconds);
				}
				return new String(value);

			}
		});
		return object;
	}

	@Override
	public Object getObject(String key, final int expiredSeconds) {
		final String keyf = (String) key;
		Object object = null;
		object = redisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] key = keyf.getBytes();
				byte[] value = connection.get(key);
				if (value == null) {
					return null;
				}
				if (expiredSeconds > 0) {
					connection.expire(key, expiredSeconds);
				}
				return ObjectUtil.toObject(value);

			}
		});
		return object;
	}

	@Override
	public Long removeObject(String key) {
		final String keyf = (String) key;
		Long number = redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] keyb = keyf.getBytes();
				return connection.del(keyb);
			}
		});
		return number;
	}

	@Override
	public void expire(String key, final int expiredSeconds) {
		final String keyf = (String) key;
		redisTemplate.execute(new RedisCallback<Object>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] key = keyf.getBytes();
				connection.expire(key, expiredSeconds);
				return 1L;
			}
		});
	}

	@Override
	public long getRedisSize() {
		long rediseSize = 0;
		rediseSize = (long) redisTemplate.execute(new RedisCallback<Object>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.dbSize();
			}
		});
		return rediseSize / 2;
	}

	@Override
	public boolean isUnLock(String key) {
		final String lock_key = key;
		Boolean isUnLock = redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				boolean isUnLock = connection.setNX(lock_key.getBytes(), "lock".getBytes());
				connection.expire(lock_key.getBytes(), 5);
				return isUnLock;
			}
		});
		return isUnLock;
	}

	@Override
	public Long ttl(String key) {
		final String keyf = (String) key;
		long ttl = 0;
		ttl = (long) redisTemplate.execute(new RedisCallback<Object>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] key = keyf.getBytes();
				return connection.ttl(key);
			}
		});
		return ttl;
	}

	@Override
	public boolean isUnLock(String key, final int expiredSeconds) {
		final String lock_key = key;
		Boolean isUnLock = redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				boolean isUnLock = connection.setNX(lock_key.getBytes(), "lock".getBytes());
				if (isUnLock) {
					connection.expire(lock_key.getBytes(), expiredSeconds);
				}
				return isUnLock;
			}
		});
		return isUnLock;
	}

	@Override
	public String lock(String key) {
		return this.lock(key, 10);
	}

	@Override
	public synchronized String lock(String key, int lockTime) {
		key = this.lockKey(key);
		String lock = UUID.randomUUID().toString().replace("-", "");
		Long endTime = System.currentTimeMillis() + 5000;
		while (System.currentTimeMillis() < endTime) {
			Long result = ((RedisTemplate<String, Object>) redisTemplate).execute(this.lockScript,
					redisTemplate.getStringSerializer(), (RedisSerializer<Long>) redisTemplate.getDefaultSerializer(),
					Collections.singletonList(key), String.valueOf(lockTime), lock);
			if (result.intValue() == 1) {
				return lock;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					continue;
				}
			}
		}
		return null;
	}

	@Override
	public boolean unlock(String key, String lock) {
		key = this.lockKey(key);
		Long result = ((RedisTemplate<String, Object>) redisTemplate).execute(this.unlockScript,
				Collections.singletonList(key), lock);
		return result.intValue() == 1;
	}

	private String lockKey(String key) {
		return key == null ? null : (key + "-LOCK");
	}

	@Override
	public Long incr(String key) {
		return incrBy(key, 0);
	}

	@Override
	public Long incrBy(final String key, final long value) {
		Long num = redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] keyb = key.getBytes();
				if (value == 0) {
					return connection.incr(keyb);
				} else {
					return connection.incrBy(keyb, value);
				}
			}
		});
		return num;
	}

	@Override
	public Long decr(String key) {
		return decrBy(key, 0);
	}

	@Override
	public Long decrBy(final String key, final long value) {
		Long num = redisTemplate.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] keyb = key.getBytes();
				if (value == 0) {
					return connection.decr(keyb);
				} else {
					return connection.decrBy(keyb, value);
				}
			}
		});
		return num;
	}

}
