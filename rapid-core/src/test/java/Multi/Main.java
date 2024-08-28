package Multi;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/27 15:46
 */
public class Main {
    public static void main(String[] args) {
        // 创建ringbuufer
        RingBuffer<Order> ringBuffer = RingBuffer.create(ProducerType.MULTI, new EventFactory<Order>() {
                    public Order newInstance() {
                        return new Order();
                    }
                },
                1024 * 1024,
                new YieldingWaitStrategy());

        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
        //3 创建多个消费者数组:
        Consumer[] consumers = new Consumer[10];
        for(int i = 0; i < consumers.length; i++) {
            consumers[i] = new Consumer("C" + i);
        }

    }
}
