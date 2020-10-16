package org.fsn.framework.redis.eshop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisStockService {

    public static final String STOCK_LUA;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    static {
        /**
         *
         * @desc 扣减库存Lua脚本
         * 库存（stock）-1：表示不限库存
         * 库存（stock）0：表示没有库存
         * 库存（stock）大于0：表示剩余库存
         *
         * @params 库存key
         * @return
         *      -3:库存未初始化
         *      -2:库存不足
         *      -1:不限库存
         *      大于等于0:剩余库存（扣减之后剩余的库存）
         *      redis缓存的库存(value)是-1表示不限库存，直接返回1
         */
        StringBuilder sb = new StringBuilder();
        sb.append("if (redis.call('exists', KEYS[1]) == 1) then");
        sb.append("    local stock = tonumber(redis.call('get', KEYS[1]));");
        sb.append("    local num = tonumber(ARGV[1]);");
        sb.append("    if (stock == -1) then");
        sb.append("        return -1;");
        sb.append("    end;");
        sb.append("    if (stock >= num) then");
        sb.append("        return redis.call('incrby', KEYS[1], 0 - num);");
        sb.append("    end;");
        sb.append("    return -2;");
        sb.append("end;");
        sb.append("return -3;");
        STOCK_LUA = sb.toString();
    }


    /**
     *  初始化 设置库存
     * @param key 库存key
     * @param num 数量
     */
    public void setStock(String key,long num){
        redisTemplate.opsForValue().set(key,String.valueOf(num));
    }

    /**
     * 添加库存
     * @param key 库存 key
     * @param num 添加数量
     */
    public void addStock(String key,long num){
        redisTemplate.opsForValue().increment(key,num);
    }


    /**
     * 获取库存
     * @param key 库存key
     * @return
     */
    public Long getStock(String key){
          return   Long.parseLong(redisTemplate.opsForValue().get(key));
    }


    /**
     * 扣库存
     *
     * @param key 库存key
     * @param num 扣减库存数量
     * @return 扣减之后剩余的库存【-3:库存未初始化; -2:库存不足; -1:不限库存; 大于等于0:扣减库存之后的剩余库存】
     */
    public Long stock(String key, int num) {
        
        long result = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {

                return  connection.eval(STOCK_LUA.getBytes(), ReturnType.INTEGER,1,key.getBytes(),String.valueOf(num).getBytes());

            }
        });
        return result;
    }

}
