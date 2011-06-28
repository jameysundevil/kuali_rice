package org.kuali.rice.test.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class used for tests within shareddata module just to find an appropriate http endpoint to publish each service on.
 * This system property is either set in the systemPropertyVariables section of the failsafe plugin's configuration
 * section or can be overridden on the java/maven invocation by passing in
 * -Djaxws.endpoint.publish.test.address=${your.url}
 */
final class ServiceEndpointLocation {
    static String ENDPOINT_URL = "http://localhost:23000/service"; //Default URL
    private static final Log LOG = LogFactory.getLog(ServiceEndpointLocation.class);
    static {
        String endpointUrl = System.getProperty("jaxws.endpointUrl.publish.test.address");
        if (endpointUrl != null) {
            ENDPOINT_URL = endpointUrl;
        }
        LOG.info("endpoint url: " + ENDPOINT_URL);
    }

    private ServiceEndpointLocation() {}
}
