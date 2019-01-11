package com.qs.rpc;

import com.qs.rpc.core.RPC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:service-provider.xml"})
public class Server {

    @Test
    public void start() {
        RPC.start();
    }
}
