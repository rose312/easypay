package com.wuswoo.easypay.main;

import com.wuswoo.easypay.common.util.MyApplicationContext;
import com.wuswoo.easypay.common.util.ThreadPoolUtil;
import com.wuswoo.easypay.http.controller.IController;
import com.wuswoo.easypay.http.controller.IControllerAdapter;
import com.wuswoo.easypay.http.server.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by wuxinjun on 16/8/31.
 */
public class EasyPaymentApp extends BaseNioServer {

    private static final Logger logger = LogManager.getLogger(EasyPaymentApp.class);

    public EasyPaymentApp() {
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        logger.info("shutting down netty");
        super.stop();
        logger.info("shutting down service thread pool");
        ThreadPoolExecutor executor = ThreadPoolUtil.getExecutor();
        if (executor != null) {
            executor.shutdown();
        }
        logger.info("shut down netty completely");
    }

    @Override
    public String serverName() {
        return "EastPaymentApp";
    }

    @Override
    protected ServerAddress getServerAddress() {
        return new ServerAddress("0.0.0.0", 12001);
    }

    @Override
    protected ChannelInitializer<SocketChannel> getChannelInitializer() {
        return new HttpPipelineInitializer(initHttpHandler());
    }

    protected RouterHandler initHttpHandler() {
        initHttpRouters();
        RouterHandler httpHandler = new RouterHandler();
        httpHandler.setAdapter(new PaymentControllerAdapter());
        return httpHandler;
    }

    protected void initHttpRouters() {
        //初始化路由
        RouterHandler.getRouter()
            .POST("/api/payments", "paymentController@postPayment")
            .POST("/api/refunds", "paymentController@postRefund")
            .GET("/api/payments/:paymentCode", "paymentController@getPayment")
            .GET("/api/refunds/:paymentCode", "paymentController@getRefund")
            .POST("/notify/payments/:platformId/:paymentCode", "notifyController@notifyPayment")
            .POST("/notify/refunds/:platformId/:paymentCode", "notifyController@notifyRefund");

    }

    public static void main(String[] args) throws Exception {
        System.out.println("user.dir:" + System.getProperty("user.dir"));
        MyApplicationContext.getInstance();
        EasyPaymentApp paymentService = new EasyPaymentApp();
        paymentService.init();
        paymentService.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(paymentService));
    }

    private class PaymentControllerAdapter implements IControllerAdapter {
        @Override
        public IController getControllerByBeanId(String beanId) {
            return (IController) MyApplicationContext.getInstance().getBean(beanId);
        }
    }

}
