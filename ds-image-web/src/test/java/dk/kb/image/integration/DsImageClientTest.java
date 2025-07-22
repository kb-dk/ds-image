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
package dk.kb.image.integration;

import java.io.IOException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Integration unittest inteded to call client methods on DsImageClient. But no methods has been define yet on the client.
 */
@Tag("integration")
public class DsImageClientTest extends IntegrationTest{
    private static final Logger log = LoggerFactory.getLogger(DsImageClientTest.class);


    @Test
    public void test() throws IOException {
        //Must be one unit test to test the setup method is working 
    }



   
}
