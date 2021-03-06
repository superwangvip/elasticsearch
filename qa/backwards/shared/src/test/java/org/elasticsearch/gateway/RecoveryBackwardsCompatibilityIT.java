/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.gateway;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESBackcompatTestCase;
import org.elasticsearch.test.ESIntegTestCase;

import java.io.IOException;

/**
 * It looks like this test asserts that Elasticsearch is able to recover the
 * files constituting the Lucene index from other Elasticsearch nodes that
 * share the same disk. This looks to mostly work but some of the files
 * consistently need to be "recovered" rather than "reused" according to the
 * stats.
 */
@ESIntegTestCase.ClusterScope(numDataNodes = 0, scope = ESIntegTestCase.Scope.TEST, numClientNodes = 0, transportClientRatio = 0.0)
public class RecoveryBackwardsCompatibilityIT extends ESBackcompatTestCase {
    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder()
                .put(super.nodeSettings(nodeOrdinal))
                .put("action.admin.cluster.node.shutdown.delay", "10ms")
                .put("gateway.recover_after_nodes", 2).build();
    }

    @Override
    protected int minExternalNodes() {
        return 2;
    }

    @Override
    protected int maxExternalNodes() {
        return 3;
    }

    public void testReusePeerRecovery() throws Exception {
        Runnable restartCluster = new Runnable() {
            @Override
            public void run() {
                try {
                    backwardsCluster().upgradeAllNodes();
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        // Backwards tests don't currently support synced flush.
        ReusePeerRecoverySharedTest.testCase(indexSettings(), restartCluster, logger, false);
    }
}
