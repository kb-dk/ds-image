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
import dk.kb.image.model.v1.DeepzoomDZIDto;
import dk.kb.image.model.v1.IIIFInfoDto;
import dk.kb.image.model.v1.ThumbnailsDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for the service. Intended for use by other projects that calls this service.
 * See the {@code README.md} for details on usage.
 * </p>
 * This class is not used internally.
 * </p>
 * The client is Thread safe and handles parallel requests independently.
 * It is recommended to persist the client and to re-use it between calls.
 */
public class DsImageClient {
    private static final Logger log = LoggerFactory.getLogger(DsImageClient.class);
    private final static String CLIENT_URL_EXCEPTION="The client url was not constructed correct";
    private final String serviceURI;
    /**
     * Creates a client for the service.
     * @param serviceURI the URI for the service, e.g. {@code https://example.com/ds-image/v1}.
     */
    public DsImageClient(String serviceURI) {
        this.serviceURI = serviceURI;
        log.info("Created OpenAPI client for '{}'", serviceURI);
    }

    //No methods enabled for java client calls yet
    
  
}
