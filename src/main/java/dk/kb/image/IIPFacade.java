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
package dk.kb.image;

import dk.kb.image.config.ServiceConfig;
import dk.kb.util.webservice.exception.InternalServiceException;
import dk.kb.util.webservice.exception.InvalidArgumentServiceException;
import dk.kb.util.webservice.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

/**
 * Proxy for an image server that supports the
 * <a href="https://iipimage.sourceforge.io/documentation/protocol/">IIP Protocol</a>.
 *
 */
public class IIPFacade {
    private static final Logger log = LoggerFactory.getLogger(IIPFacade.class);

    private static IIPFacade instance;

    public static final String KEY_IIP_SERVER = "imageservers.iip.server";

    public static final String KEY_DEEPZOOM_SERVER_PATH = "imageservers.deepzoom.path";

    public static final String KEY_DEEPZOOM_SERVER_PARAM = "imageservers.deepzoom.param";

    public static synchronized IIPFacade getInstance() {
        if (instance == null) {
            instance = new IIPFacade();
        }
        return instance;
    }

    private IIPFacade() {
        log.info("Created " + this);
    }


    /**
     * Internet Imaging Protocol available through OpenAPI specification. The specification can be found at: https://iipimage.sourceforge.io/documentation/protocol/
     *
     * @param requestURI full request URI, used for logging af debugging.
     *
     * @param FIF: Full image path. If the FILESYSTEM_PREFIX server parameter has been set, the path is relative to that path.  Note that all IIP protocol requests must start with the FIF parameter
     *
     * @param WID: Specify width in pixels, w, for exports with CVT request.  **WID requires CVT**
     *
     * @param HEI: Specify height in pixels, h, for exports with CVT requests  **HEI requires CVT**
     *
     * @param RGN: Define a region of interest starting at relative coordinates x,y with width w and height h.  For use with CVT requests. All values should be ratios in the range 0 – 1.0    **RGN has to be defined as x,y,w,h and requires CVT, WID &amp; HEI**
     *
     * @param QLT: Set the output compression level, q.    Valid ranges are JPEG: 0-100 and for PNG: 0-9 where a higher value means more compression  JPEG is always lossy even with a value of 100, while PNG is lossles.
     *
     * @param CNT: Contrast adjustment: multiplication of pixel values by factor, c. Value should be an integer or float &gt; 0. A value of 1.0 indicates no contrast change
     *
     * @param GAM: Apply gamma correction, g: each pixel value to the power of g.  If g&#x3D;log or g&#x3D;logarithm, the logarithm is applied
     *
     * @param CMP: Generate colormap using one of the standard colormap schemes, s: GREY, JET, COLD, HOT, RED, GREEN and BLUE.
     *
     * @param PFL: Export profile in JSON format at resolution r from position x1,y1 to x2, y2.  Only horizontal or vertical profiles are currently supported.  **PFL has to be defined specifically as r:x1,y1-x2,y2**  **An example: 800:20,20-440,440**
     *
     * @param CTW: Color twist / channel recombination. Recombine the available image channels into a new color image by multiplication through a matrix. Columns are separated by commas and rows are separated by semi-colons. Values can also be negative.  Thus, for the 3×3 matrix example provided below, the RGB output image will have bands R &#x3D; R*r1 + G*g1 + B*b1, G &#x3D; R*r2 + G*g2 + B*b2, B &#x3D; R*r3 + G*g3 + B*b3.  For multi-band images, the row length should correspond to the number of available bands within the image. The number of output bands depends on the number of rows in the matrix. Thus, to output a 1 band greyscale image, specify just a single row.  Examples: To perform naive conversion from 3 channel color to 1 channel grayscale: CTW&#x3D;[0.33,0.33,0.33]  To flip the R and B channels and map an RGB image to BGR: CTW&#x3D;[0,0,1;0,1,0;1,0,0]  For a 5-band multispectral image, to show the difference between the 5th and 2nd band (i.e. 5th-2nd) and outputting the result as grayscale: CTW&#x3D;[0,-1,0,0,1]  To create a false-color image from a 4-band RGB-IR image by mapping the G,R,IR channels to the output RGB: CTW&#x3D;[0,1,0,0;0,0,1,0;0,0,0,1]  **CTW has to be defined as [array;array;array] using ; as delimter between arrays and , between integers**
     *
     * @param INV: Invert image (no argument)
     *
     * @param COL: Color transformation to output space, c. Valid values are greyscale (GREY or GRAY) or to binary (BINARY).   Examples: Convert to greyscale: COL&#x3D;gray  Convert to binary: COL&#x3D;binary
     *
     * @param JTL: Return a tile in JPEG format with index n at resolution level r  **JTL has to be specified as r,n**
     *
     * @param PTL: Return a tile in PNG format at resolution level r with index n    **PTL has to be specified as r,n**
     *
     * @param CVT: Export the full image or a region in the specified format (JPEG and PNG currently supported)
     *
     * @param httpHeaders the original httpHeaders from the client. Used to transfer specific header fields to image server request.
     * 
     * @return a bitmap.
     * @throws ServiceException when other http codes should be returned
     */
    public javax.ws.rs.core.StreamingOutput getIIPImage(
            URI requestURI,
            String FIF, Long WID, Long HEI, List<Float> RGN, Integer QLT, Float CNT,
            String ROT, Float GAM, String CMP, String PFL, String CTW, Boolean INV, String COL,
            List<Integer> JTL, List<Integer> PTL, String CVT, HttpHeaders httpHeaders) throws ServiceException {

        IIPParamValidation.validateIIPRequest(FIF, WID, HEI, RGN, QLT, CNT, ROT, GAM, CMP, PFL, CTW, INV, COL, JTL, PTL, CVT);

        // Defaults
        if (CVT == null || "jpg".equals(CVT)) {
            CVT = "jpeg";
        }

        // TODO: Use the UriTemplate system like IIIFFacade
        // http://example.com/iipsrv.fcgi?FIF=/mymount/85/c1/85c1df89-bffe-48e0-8813-111f6f0fba50.jp2&CVT=jpeg
        UriBuilder builder = UriBuilder.
                fromUri(ServiceConfig.getConfig().getString(KEY_IIP_SERVER)).
                queryParam("FIF", FIF); // Mandatory
        ProxyHelper.addIfPresent(builder, "WID", WID);
        ProxyHelper.addIfPresent(builder, "HEI", HEI);
        ProxyHelper.addIfPresent(builder, "RGN", RGN);
        ProxyHelper.addIfPresent(builder, "QLT", QLT);
        ProxyHelper.addIfPresent(builder, "CNT", CNT);

        ProxyHelper.addIfPresent(builder, "ROT", ROT);
        ProxyHelper.addIfPresent(builder, "GAM", GAM);
        ProxyHelper.addIfPresent(builder, "CMP", CMP);
        ProxyHelper.addIfPresent(builder, "PFL", PFL);
        ProxyHelper.addIfPresent(builder, "CTW", CTW);
        ProxyHelper.addIfPresent(builder, "INV", INV); // TODO: Figure out how to add INV as a value-less param
        ProxyHelper.addIfPresent(builder, "COL", COL);

        ProxyHelper.addIfPresent(builder, "JTL", JTL);
        ProxyHelper.addIfPresent(builder, "PTL", PTL);
        ProxyHelper.addIfPresent(builder, "CVT", CVT);

        final URI uri = builder.build();

        return ProxyHelper.proxy(FIF, uri, requestURI, httpHeaders);
    }

    public javax.ws.rs.core.StreamingOutput getDeepzoomDZI(
            URI requestURI, String imageid,
            HttpServletResponse httpServletResponse,  HttpHeaders httpHeaders) throws ServiceException {
        validateDeepzoomDZIRequest(imageid);
        final String idDZI = imageid + (imageid.endsWith(".dzi") ? "" : ".dzi");
        // Defaults
        UriBuilder builder;
        if (ServiceConfig.getConfig().containsKey(KEY_DEEPZOOM_SERVER_PATH)){
            builder = UriBuilder.
            fromUri(ServiceConfig.getConfig().getString(KEY_DEEPZOOM_SERVER_PATH)).
            path(idDZI); // Mandatory
        } 
        else if (ServiceConfig.getConfig().containsKey(KEY_DEEPZOOM_SERVER_PARAM)){
            builder = UriBuilder.
            fromUri(ServiceConfig.getConfig().getString(KEY_DEEPZOOM_SERVER_PARAM)).
            queryParam("DeepZoom", idDZI); // Mandatory
        }
        else {
            log.error("No Deepzoom server defined");
            throw new InternalServiceException("No Deepzoom server defined");
        }
        // TODO: Use the UriTemplate system like IIIFFacade
        // http://example.com//fcgi-bin/iipsrv.fcgi?DeepZoom=/your/image/path.tif.dzi

        final URI uri = builder.build();

        return ProxyHelper.proxy(imageid, uri, requestURI, httpServletResponse, httpHeaders);
    }

    public javax.ws.rs.core.StreamingOutput getDeepzoomTile(
            URI requestURI,
            String imageid, Integer layer, String tiles, String format, Float CNT,
            Float GAM, String CMP, String CTW, Boolean INV, String COL, HttpHeaders httpHeaders) throws ServiceException {
        IIPParamValidation.validateDeepzoomTileRequest(imageid, layer, tiles, format, CNT, GAM, CMP, CTW, INV, COL);

        // Defaults
        if (format == null) {
            format = "jpeg";
        }

        // TODO: Use the UriTemplate system like IIIFFacade
        // http://example.com//fcgi-bin/iipsrv.fcgi?DeepZoom=/your/image/path.tif.dzi
        UriBuilder builder;
        if (ServiceConfig.getConfig().containsKey(KEY_DEEPZOOM_SERVER_PATH)){
            builder = UriBuilder.
            fromUri(ServiceConfig.getConfig().getString(KEY_DEEPZOOM_SERVER_PATH)).
            path(imageid + "_files").path("" + layer).path(tiles + "." + format);
        } 
        else if (ServiceConfig.getConfig().containsKey(KEY_DEEPZOOM_SERVER_PARAM)){
            builder = UriBuilder.
            fromUri(ServiceConfig.getConfig().getString(KEY_DEEPZOOM_SERVER_PARAM)).
            queryParam("DeepZoom", imageid + "_files/" + layer + "/" + tiles + "." + format);
        }
        else {
            log.error("No Deepzoom server defined");
            throw new InternalServiceException("No Deepzoom server defined");
        }

        ProxyHelper.addIfPresent(builder, "layer", layer);
        ProxyHelper.addIfPresent(builder, "tiles", tiles);
        ProxyHelper.addIfPresent(builder, "format", format);
        ProxyHelper.addIfPresent(builder, "CNT", CNT);

        ProxyHelper.addIfPresent(builder, "GAM", GAM);
        ProxyHelper.addIfPresent(builder, "CMP", CMP);
        ProxyHelper.addIfPresent(builder, "CTW", CTW);
        ProxyHelper.addIfPresent(builder, "INV", INV); // TODO: Figure out how to add INV as a value-less param
        ProxyHelper.addIfPresent(builder, "COL", COL);

        final URI uri = builder.build();

        return ProxyHelper.proxy(imageid, uri, requestURI,httpHeaders);
    }

    /**
     * Validates Deepzoom DZI parameters and throws appropriate exceptions if any are invalid.
     * See https://iipimage.sourceforge.io/documentation/protocol/
     * The documentation is very subtle on Deepzoom. One could also look at OpenSeadragon documentation
     * https://openseadragon.github.io/docs/
     * @throws ServiceException thrown if any parameters are not conforming to the IIP specification.
     */
    private void validateDeepzoomDZIRequest(
            String imageid) {
        if (imageid == null || imageid.isEmpty()) {
            throw new InvalidArgumentServiceException("The parameter imageid must be defined");
        }
    }

}
