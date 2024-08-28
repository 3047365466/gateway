package Multi;

import com.lmax.disruptor.WorkHandler;
import lombok.Data;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/27 15:44
 */
@Data
public class Consumer implements WorkHandler<Order> {
    private String comsumerId;

    private Random random = new Random();

    // 挂在类上，用于统计消费次数
    private static AtomicInteger count = new AtomicInteger(0);

    @Override
    public void onEvent(Order event) throws Exception {
        Thread.sleep(1 * random.nextInt(5));
        System.err.println("当前消费者: " + this.comsumerId + ", 消费信息ID: " + event.getId());
        count.incrementAndGet();
    }
    public Consumer(String comsumerId) {
        this.comsumerId = comsumerId;
    }
}
