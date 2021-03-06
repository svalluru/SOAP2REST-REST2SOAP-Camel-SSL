/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.example.soap;


import static org.junit.Assert.assertTrue;

import org.arquillian.cube.kubernetes.api.Session;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;

@RunWith(Arquillian.class)
@RunAsClient
public class KubernetesIntegrationKT {

    @ArquillianResource
    KubernetesClient client;

    @ArquillianResource
    public Session session;

    @Test
    public void testAppProvisionsRunningPods() throws Exception {
        boolean foundReadyPod = false;

        PodList podList = client.pods().inNamespace(session.getNamespace()).list();
        for (Pod p : podList.getItems()) {
            if (!p.getMetadata().getName().endsWith("build") && !p.getMetadata().getName().endsWith("deploy")) {
                assertTrue(p.getMetadata().getName() + " is not ready", Readiness.isReady(p));
                foundReadyPod = true;
            }
        }
        assertTrue("Found no ready pods in namespace", foundReadyPod);
    }
}
