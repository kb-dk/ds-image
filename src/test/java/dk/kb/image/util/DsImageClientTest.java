/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.image.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple verification of client code generation.
 */
public class DsImageClientTest {
    private static final Logger log = LoggerFactory.getLogger(DsImageClientTest.class);

    // We cannot test usage as that would require a running instance of ds-image to connect to
    @Test
    public void testInstantiation() {
        String backendURIString = "htp://example.com/ds-image/v1";
        log.debug("Creating inactive client for ds-image with URI '{}'", backendURIString);
        new DsImageClient(backendURIString);
    }
}
