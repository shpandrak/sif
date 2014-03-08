package com.shpandrak.integrationtests;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.shpandrak.tests.common.ScenariosHelper;
import com.shpandrak.world.gae.datastore.WorldModuleGoogleDatastoreLayerLoader;
import com.shpandrak.world.model.WorldModuleLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/1/13
 * Time: 11:39
 */
public class TestGAEScenarios {

/*
    // maximum eventual consistency
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));
*/
    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new  LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy());

    @Before
    public void initTest() throws Exception{
        helper.setUp();
        WorldModuleLoader.load();
        WorldModuleGoogleDatastoreLayerLoader.load();
    }
    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testList() throws Exception {

        ScenariosHelper.runTestCode();

    }
}
