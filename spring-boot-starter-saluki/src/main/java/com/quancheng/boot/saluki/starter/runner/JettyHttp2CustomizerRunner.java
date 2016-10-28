package com.quancheng.boot.saluki.starter.runner;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;

public class JettyHttp2CustomizerRunner implements EmbeddedServletContainerCustomizer {

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        if (container instanceof JettyEmbeddedServletContainerFactory) {
            JettyEmbeddedServletContainerFactory factory = (JettyEmbeddedServletContainerFactory) container;

        }

    }

}
