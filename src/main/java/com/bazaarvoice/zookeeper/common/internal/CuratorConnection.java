package com.bazaarvoice.zookeeper.common.internal;

import com.bazaarvoice.zookeeper.common.ZooKeeperConnection;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.curator.RetryPolicy;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <b>NOTE: This is an INTERNAL class to the SOA library.  You should not be using this directly!!!!</b>
 *
 * This class provides a ZooKeeperConnection that is backed by Netflix's Curator library.  This class is in an internal
 * package to avoid users from using it.  Curator should not appear anywhere in the public interface of the SOA library.
 *
 * @see com.bazaarvoice.zookeeper.common.ZooKeeperConfiguration
 */
public class CuratorConnection implements ZooKeeperConnection {
    private final CuratorFramework _curator;

    public CuratorConnection(String connectString, RetryPolicy retryPolicy, String namespace) {
        checkNotNull(connectString);
        checkNotNull(retryPolicy);

        // An empty namespace means no namespace, in which case Curator expects namespace==null.
        if ("".equals(namespace)) {
            namespace = null;
        }

        // Make all of the curator threads daemon threads so they don't block the JVM from terminating.
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("CuratorFramework[" + connectString + "]-%d")
                .setDaemon(true)
                .build();

        try {
            _curator = CuratorFrameworkFactory.builder()
                    .connectString(connectString)
                    .retryPolicy(retryPolicy)
                    .namespace(namespace)
                    .threadFactory(threadFactory)
                    .build();
            _curator.start();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public CuratorFramework getCurator() {
        return _curator;
    }

    @Override
    public void close() throws IOException {
        _curator.close();
    }
}
