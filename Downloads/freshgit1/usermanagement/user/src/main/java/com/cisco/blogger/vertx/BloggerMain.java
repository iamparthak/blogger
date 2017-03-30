package com.cisco.blogger.vertx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.cisco.blogger.config.ApplicationConfiguration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;


public class BloggerMain {
	public static void main(String[] args) {
				
		ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
		Config hazelcastConfig = new Config();


        hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
        hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
		//final Vertx vertx = Vertx.factory.vertx();
         vertx.deployVerticle(new UserVerticle(context));
         vertx.deployVerticle(new DatabaseVerticle(context));
            }
        });
	}
}
