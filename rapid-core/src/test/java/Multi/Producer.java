package Multi;

import com.lmax.disruptor.RingBuffer;
import lombok.Data;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/27 15:40
 */
@Data
public class Producer {

    private RingBuffer<Order> ringBuffer;

    public Producer(RingBuffer<Order> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void sendData(String uuid) {
        long sequence = ringBuffer.next();

        try {
            Order order = ringBuffer.get(sequence);
            order.setId(uuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
