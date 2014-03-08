package com.shpandrak.integrationtests;

import com.shpandrak.persistence.IConnectionProvider;
import com.shpandrak.persistence.IConnectionProviderFactory;
import com.shpandrak.persistence.PersistenceLayerManager;
import com.shpandrak.tests.common.ScenariosHelper;
import com.shpandrak.world.db.WorldModuleDatabaseLayerLoader;
import com.shpandrak.world.model.WorldModuleLoader;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.webapp.WebAppContext;
import org.h2.tools.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/1/13
 * Time: 11:39
 */
public class TestItBaby {

    private Server server;
    private org.eclipse.jetty.server.Server jettyServer;

    @Before
    public void initTest() throws Exception{
        WorldModuleLoader.load();
        WorldModuleDatabaseLayerLoader.load();
        PersistenceLayerManager.init(new IConnectionProviderFactory() {
            @Override
            public IConnectionProvider create() {
                return new TestJDBCConnectionProvider();
            }
        });

        server = Server.createTcpServer().start();

        // Getting the schema file preparing the database for the test
        File schemaFile = new File("./src/main/resources/schema.sql");
        System.out.println(schemaFile.getAbsolutePath());
        TestJDBCConnectionProvider cp = new TestJDBCConnectionProvider();
        Connection conn = cp.getConnection();
        try{
            Statement statement = conn.createStatement();
            statement.close();
            ScriptRunner scriptRunner = new ScriptRunner(conn, true, true);
            scriptRunner.runScript(new FileReader(schemaFile));

        }finally {
            cp.returnConnection(conn);
        }


        jettyServer = new org.eclipse.jetty.server.Server(8066);
        WebAppContext context = new WebAppContext();
        context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        context.setResourceBase("src/main/webapp");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);
        jettyServer.setHandler(context);
        jettyServer.start();

        int idx = 0;
        while (!jettyServer.isStarted() && idx < 20){
            Thread.sleep(500);
            idx++;
        }
        //jettyServer.join();

    }
    @After
    public void tearDown() throws Exception {
        if (server != null){
            server.stop();
        }
        if (jettyServer != null){
            jettyServer.stop();
            jettyServer.join();
        }
    }

    @Test
    public void testList() throws Exception {


            ScenariosHelper.runTestCode();

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(
                    "http://localhost:8066/rest/country");
            //getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            httpClient.getConnectionManager().shutdown();






    }

}
