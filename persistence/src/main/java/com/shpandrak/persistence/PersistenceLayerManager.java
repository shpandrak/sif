package com.shpandrak.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/20/13
 * Time: 09:15
 */
public abstract class PersistenceLayerManager {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceLayerManager.class);
    private static final ThreadLocal<ConnectionInfo> threadLocal = new ThreadLocal<ConnectionInfo>();
    private static IConnectionProviderFactory connectionProviderFactory;

    public static void init(IConnectionProviderFactory connectionProviderFactory) {
        PersistenceLayerManager.connectionProviderFactory = connectionProviderFactory;
    }

    public static boolean hasActiveSession(){
        return threadLocal.get() != null;
    }

    /**
     * Cleaning up connectionProvider even regardless of the current state and number of callers
     * This call will close the active connectionProvider if any and treat current thread as free for new connections
     */
    public static void cleanUp() {
        ConnectionInfo connectionInfo = threadLocal.get();
        if (connectionInfo == null){
            logger.debug("Cleanup connectionProvider for thread {} - nothing to do - no active connectionProvider", Thread.currentThread().getName());
        }else {
            logger.debug("Cleaning up connectionProvider for thread {} with {} callers", Thread.currentThread().getName(), connectionInfo.getNumberOfCallers());
            closeConnectionAndCleanTheadLocal(connectionInfo.getConnectionProvider());
        }
    }


    public static void beginOrJoinConnectionSession(){
        ConnectionInfo connectionInfo = threadLocal.get();
        if (connectionInfo != null){
            logger.debug("Joining an existing connectionProvider session for thread {}", Thread.currentThread().getName());
            connectionInfo.increaseCallers();
        }else {
            beginConnectionSession();
        }
    }

    public static void endJointConnectionSession() {
        ConnectionInfo connectionInfo = threadLocal.get();
        if (connectionInfo == null){
            throw new IllegalStateException("endJointConnectionSession called on a thread not associated with any connectionProvider: " + Thread.currentThread().getName());
        }

        if (connectionInfo.getNumberOfCallers() == 1){
            finishUpConnectionInfo(connectionInfo);
        }else {
            logger.debug("Decreasing connectionProvider callers number from {} on thread {}", connectionInfo.getNumberOfCallers(), Thread.currentThread().getName());
            connectionInfo.decreaseCallers();
        }
    }

    /**
     * Initializing connectionProvider info without actually opening a connectionProvider. this is useful to prevent
     * Connection closing every time requested in thread. while a connectionProvider session is active, all calls to getConnectionProvider
     * method will use the same connectionProvider if opened
     */
    public static void beginConnectionSession(){
        ConnectionInfo connectionInfo = threadLocal.get();
        if (connectionInfo != null){
            throw new IllegalStateException("A connectionProvider session is already defined for this thread: " + Thread.currentThread().getName());
        }

        // Initializing connectionProvider info without actually opening a connectionProvider
        logger.debug("Initializing connectionProvider session for thread {}", Thread.currentThread().getName());
        ConnectionInfo info = new ConnectionInfo();
        threadLocal.set(info);
    }

    public void endConnectionSession() {
        ConnectionInfo connectionInfo = threadLocal.get();
        if (connectionInfo == null){
            throw new IllegalStateException("endConnectionSession called on a thread not associated with any connectionProvider: " + Thread.currentThread().getName());
        }

        if (connectionInfo.getNumberOfCallers() == 1){
            finishUpConnectionInfo(connectionInfo);
        }else {
            if (connectionInfo.getConnectionProvider() == null){
                logger.warn("endConnectionSession called on a thread while connectionProvider has not been returned. thread: " +
                        Thread.currentThread().getName() + " callers count: " + connectionInfo.getNumberOfCallers() + " No actual connectionProvider was opened for this thread");
                threadLocal.remove();
            }else {
                logger.warn("endConnectionSession called on a thread while connectionProvider has not been returned. thread: " +
                        Thread.currentThread().getName() + " callers count: " + connectionInfo.getNumberOfCallers());
                closeConnectionAndCleanTheadLocal(connectionInfo.getConnectionProvider());
            }
        }
    }

    private static void finishUpConnectionInfo(ConnectionInfo connectionInfo) {
        if (connectionInfo.getConnectionProvider() == null){
            logger.debug("Clearing connectionProvider info for thread {}. connectionProvider has never been opened for this thread", Thread.currentThread().getName());
            threadLocal.remove();
        }else {
            logger.debug("Closing a connectionProvider for thread {}", Thread.currentThread().getName());
            closeConnectionAndCleanTheadLocal(connectionInfo.getConnectionProvider());
        }
    }


    public static IConnectionProvider getConnectionProvider() {
        ConnectionInfo connectionInfo = threadLocal.get();
        IConnectionProvider connectionProvider;
        if (connectionInfo == null){
            logger.debug("Creating a connectionProvider provider for thread {}", Thread.currentThread().getName());
            connectionProvider = createNewConnectionProvider();
            ConnectionInfo info = new ConnectionInfo(connectionProvider);
            threadLocal.set(info);

        }else if (connectionInfo.getConnectionProvider() == null) {
            logger.debug("Found an existing non-initialized connectionProvider on thread {}, Initializing connectionProvider", Thread.currentThread().getName());
            connectionProvider = createNewConnectionProvider();
            connectionInfo.setConnectionProvider(connectionProvider);
            //connectionInfo.increaseCallers();
        }else {
            //connectionInfo.increaseCallers();
            logger.debug("Found an existing connectionProvider on thread {}, Thread callers: {}", Thread.currentThread().getName(), connectionInfo.getNumberOfCallers());
            connectionProvider = connectionInfo.getConnectionProvider();
        }

        return connectionProvider;

    }

    private static IConnectionProvider createNewConnectionProvider(){
        return connectionProviderFactory.create();
    }

    private static void closeConnectionAndCleanTheadLocal(IConnectionProvider connectionProvider) {
        try{
            connectionProvider.destroy();
        }finally {
            threadLocal.remove();
        }
    }


    private static class ConnectionInfo{
        private IConnectionProvider connectionProvider;
        private int numberOfCallers;

        private ConnectionInfo() {
            this.connectionProvider = null;
            this.numberOfCallers = 1;
        }

        private ConnectionInfo(IConnectionProvider connectionProvider) {
            this.connectionProvider = connectionProvider;
            this.numberOfCallers = 1;
        }

        public IConnectionProvider getConnectionProvider() {
            return connectionProvider;
        }

        public void setConnectionProvider(IConnectionProvider connectionProvider) {
            this.connectionProvider = connectionProvider;
        }

        public int getNumberOfCallers() {
            return numberOfCallers;
        }

        public void increaseCallers(){
            this.numberOfCallers++;
        }

        public void decreaseCallers(){
            this.numberOfCallers--;
        }
    }


}
