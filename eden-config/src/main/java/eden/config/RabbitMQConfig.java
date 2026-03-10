package eden.config;

import eden.common.constant.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMQConfig {

    /**
     * JSON消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    // ========================= 订单模块 =========================

    /**
     * 订单Direct交换机
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(MQConstants.ORDER_EXCHANGE, true, false);
    }

    /**
     * 订单创建队列
     */
    @Bean
    public Queue orderCreateQueue() {
        return new Queue(MQConstants.ORDER_CREATE_QUEUE, true);
    }

    /**
     * 订单取消队列
     */
    @Bean
    public Queue orderCancelQueue() {
        return new Queue(MQConstants.ORDER_CANCEL_QUEUE, true);
    }

    /**
     * 订单支付成功队列
     */
    @Bean
    public Queue orderPaySuccessQueue() {
        return new Queue(MQConstants.ORDER_PAY_SUCCESS_QUEUE, true);
    }

    /**
     * 订单创建队列绑定
     */
    @Bean
    public Binding orderCreateBinding(Queue orderCreateQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreateQueue).to(orderExchange).with(MQConstants.ORDER_CREATE_ROUTING_KEY);
    }

    /**
     * 订单取消队列绑定
     */
    @Bean
    public Binding orderCancelBinding(Queue orderCancelQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCancelQueue).to(orderExchange).with(MQConstants.ORDER_CANCEL_ROUTING_KEY);
    }

    /**
     * 订单支付成功队列绑定
     */
    @Bean
    public Binding orderPaySuccessBinding(Queue orderPaySuccessQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderPaySuccessQueue).to(orderExchange).with(MQConstants.ORDER_PAY_SUCCESS_ROUTING_KEY);
    }

    // ========================= 延迟订单关闭 =========================

    /**
     * 订单延迟交换机（使用TTL + 死信队列实现）
     */
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(MQConstants.ORDER_DELAY_EXCHANGE, true, false);
    }

    /**
     * 订单延迟队列（TTL队列，消息过期后进入死信队列）
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 消息过期后转发到订单交换机
        args.put("x-dead-letter-exchange", MQConstants.ORDER_EXCHANGE);
        // 转发到订单取消队列
        args.put("x-dead-letter-routing-key", MQConstants.ORDER_CANCEL_ROUTING_KEY);
        // 消息过期时间30分钟
        args.put("x-message-ttl", 30 * 60 * 1000);
        return new Queue(MQConstants.ORDER_DELAY_QUEUE, true, false, false, args);
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding orderDelayBinding(Queue orderDelayQueue, DirectExchange orderDelayExchange) {
        return BindingBuilder.bind(orderDelayQueue).to(orderDelayExchange).with(MQConstants.ORDER_DELAY_ROUTING_KEY);
    }

    // ========================= 秒杀模块 =========================

    /**
     * 秒杀Direct交换机
     */
    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(MQConstants.SECKILL_EXCHANGE, true, false);
    }

    /**
     * 秒杀订单队列
     */
    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(MQConstants.SECKILL_ORDER_QUEUE, true);
    }

    /**
     * 秒杀订单队列绑定
     */
    @Bean
    public Binding seckillOrderBinding(Queue seckillOrderQueue, DirectExchange seckillExchange) {
        return BindingBuilder.bind(seckillOrderQueue).to(seckillExchange).with(MQConstants.SECKILL_ORDER_ROUTING_KEY);
    }

    // ========================= 库存模块 =========================

    /**
     * 库存Direct交换机
     */
    @Bean
    public DirectExchange stockExchange() {
        return new DirectExchange(MQConstants.STOCK_EXCHANGE, true, false);
    }

    /**
     * 库存扣减队列
     */
    @Bean
    public Queue stockDeductQueue() {
        return new Queue(MQConstants.STOCK_DEDUCT_QUEUE, true);
    }

    /**
     * 库存回滚队列
     */
    @Bean
    public Queue stockRollbackQueue() {
        return new Queue(MQConstants.STOCK_ROLLBACK_QUEUE, true);
    }

    /**
     * 库存扣减队列绑定
     */
    @Bean
    public Binding stockDeductBinding(Queue stockDeductQueue, DirectExchange stockExchange) {
        return BindingBuilder.bind(stockDeductQueue).to(stockExchange).with(MQConstants.STOCK_DEDUCT_ROUTING_KEY);
    }

    /**
     * 库存回滚队列绑定
     */
    @Bean
    public Binding stockRollbackBinding(Queue stockRollbackQueue, DirectExchange stockExchange) {
        return BindingBuilder.bind(stockRollbackQueue).to(stockExchange).with(MQConstants.STOCK_ROLLBACK_ROUTING_KEY);
    }
}
