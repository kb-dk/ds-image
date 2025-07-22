package dk.kb.image;

import dk.kb.util.webservice.exception.InvalidArgumentServiceException;
import dk.kb.util.webservice.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parameter validation for the Internet Imaging Protocol. The protocol can be found : <a href="https://iipimage.sourceforge.io/documentation/protocol/">here</a>.
 * Each method in this class has been made from this specification.
 */
public class IIPParamValidation {
    private static final Logger log = LoggerFactory.getLogger(IIPParamValidation.class);

    /**
     * Validates IIP parameters and throws appropriate exceptions if any parameters are invalid.
     * See <a href="https://iipimage.sourceforge.io/documentation/protocol/">the protocol</a>
     * @throws ServiceException if any parameters are not conforming to the IIP specification.
     */
    public static void validateIIPRequest(
            String fif, Long wid, Long hei, List<Float> rgn, Integer qlt, Float cnt,
            String rot, Float gam, String cmp, String pfl, String ctw, Boolean inv, String col,
            List<Integer> jtl, List<Integer> ptl, String cvt) {
        // Validates fif param
        fifValidation(fif);
        // Validate that only one of CVT, JTl and PTl are set.
        validateOneJtlPtlCvtExists(jtl, ptl, cvt);
        // Validates CVT param
        cvtValidation(cvt);
        // Validation of JTL
        jtlValidation(jtl);
        // Validation of PTL
        ptlValidation(ptl);
        // Validation of WID
        widValidation(wid, cvt);
        // Validation of HEI
        heiValidation(hei, cvt);
        // Validation of RGN
        rgnValidation(rgn, cvt);
        // Validation of QLT
        qltValidation(qlt, cvt);
        // Validation of CNT
        cntValidation(cnt);
        // Validation of ROT
        rotValidation(rot);
        // Validation of CMP
        cmpValidation(cmp);
        // Validation of PFL
        pflValidation(pfl);
        // TODO: Perform validation of MINMAX, which is not in our proxy yet
        // Validation of CTW
        ctwValidation(ctw);
        // Validation of COL
        colValidation(col);
    }

    /**
     * Validates Deepzoom parameters and throws appropriate exceptions if any are invalid.
     * See <a href="https://iipimage.sourceforge.io/documentation/protocol/">the docs</a>
     * The documentation is very subtle on Deepzoom. One could also look at OpenSeadragon
     * <a href="https://openseadragon.github.io/docs/">documentation</a>
     * @throws ServiceException thrown if any parameters are not conforming to the IIP specification.
     */
    static void validateDeepzoomTileRequest(
            String imageid, Integer layer, String tiles, String format, Float CNT,
            Float GAM, String CMP, String CTW, Boolean INV, String COL) {
        if (imageid == null || imageid.isEmpty()) {
            throw new InvalidArgumentServiceException("The parameter imageid must be defined");
        }
        // Layer is an integer. Not sure it this has to be validated against a maximum number maybe?
        // Validate tile parameter
        deepzoomTileValidation(tiles);
        // Validate tile output format
        deepzoomFormatValidation(format);
        // Validate CNT
        cntValidation(CNT);
        // Validate CMP
        cmpValidation(CMP);
        // Validate CTW
        ctwValidation(CTW);
        // Validate COL
        colValidation(COL);
        // TODO: Perform validation of all parameters
    }


    /**
     * Validate that FIF is set  and isn't empty as it is required in IIP
     */
    public static void fifValidation(String fif){
        if (fif == null || fif.isEmpty()) {
            throw new InvalidArgumentServiceException("The parameter FIF must be defined");
        }
    }

    /**
     * Validate that CVT is set to either jpeg or png, as these are the allowed types for export in IIP
     */
    public static void cvtValidation(String cvt){
        if (cvt != null) {
            if (!(cvt.equals("jpeg") || cvt.equals("png"))) {
                // Maybe add a fallback to either one of them here?
                throw new InvalidArgumentServiceException(
                        "The parameter CVT must be defined and must be either 'jpeg' or 'png'. It was '" + cvt + "'");
            }
        }
    }

    /**
     * Validate that JTL is correctly set, when exporting JPEG tiles.
     * JTL should contain index n at resolution level r, specified as r,n.
     * For example: 5,800
     */
    public static void jtlValidation(List<Integer> jtl){
        if (jtl != null && !jtl.isEmpty()) {
            if (jtl.size() != 2){
                throw new InvalidArgumentServiceException("The parameter JTL has to contain two values index x and resolution level r. Input was: '" + jtl + "'");
            }
            // TODO: If JTL (or PTL below) is defined as three or more the program throws a "Class java.lang.Integer can not be instantiated using a constructor with a single String argument"
            // I am not sure if this is due to the max size of the list as defined in our openAPI YAML
        }
    }

    /**
     * Validate that PTL is correctly set, when exporting PNG tiles.
     * PTL should contain index n at resolution level r, specified as r,n.
     * For example: 5,800
     */
    public static void ptlValidation(List<Integer> ptl) {
        if (ptl != null && !ptl.isEmpty()) {
            if (ptl.size() < 2) {
                throw new InvalidArgumentServiceException("The parameter PTL has to contain two values index x and resolution level r. Input was: '" + ptl + "'");
            }
            else if (ptl.size() > 2) {
                log.warn("PTL contains more than 2 values. PTL can only contain two values: index x and resolution level r. Input was: '{}'", ptl);
                throw new InvalidArgumentServiceException("The parameter PTL has to contain two values index x and resolution level r. Input was: '" + ptl + "'");
            }
        }
    }

    /**
     * The IIP protocol requires one and only one of JTL, PTL and CVT to be set. This method validates that only one of these are indeed set.
     */
    public static void validateOneJtlPtlCvtExists(List<Integer> jtl, List<Integer> ptl, String cvt){
        boolean jtlPresent = (jtl != null && !jtl.isEmpty());
        boolean ptlPresent = (ptl != null && !ptl.isEmpty());
        boolean cvtPresent = (cvt != null && !cvt.isEmpty() && !cvt.isBlank());

        boolean[] valuesPresent = new boolean[]{jtlPresent,ptlPresent,cvtPresent};

        if (areMoreThanOneTrue(valuesPresent)){
            log.error("More than one of the parameters JTL, PTL and CVT are set. Only one can be set at a time");
            throw new InvalidArgumentServiceException("More than one of the parameters JTL, PTL and CVT are set. Only one can be set at a time");
        }
    }

    private static boolean areMoreThanOneTrue(boolean[] arrayOfBools) {
        int trueBooleans = 0;
        for (boolean i : arrayOfBools) {
            trueBooleans += i ? 1 : 0;
            if (trueBooleans >= 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate that WID(width) is only set, when used together with CVT.
     * WID has to be set as width in pixels.
     * For example 1200
     */
    public static void widValidation(Long wid, String cvt) {
        if ((cvt == null || cvt.isEmpty()) && wid != null) {
            throw new InvalidArgumentServiceException("The parameter WID is only to be set, when the parameter CVT is in use");
        }
    }

    /**
     * Validate that HEI(height) is only set, when used together with CVT.
     * HEI has to be set as height in pixels.
     * For example 800
     */
    public static void heiValidation(Long hei, String cvt) {
        if ((cvt == null || cvt.isEmpty()) && hei != null) {
            throw new InvalidArgumentServiceException("The parameter HEI is only to be set, when the parameter CVT is in use");
        }
    }

    /**
     * Validate that RGN(region) is correctly set, by validating each element of the list.
     * RGN has to be defined as x,y,w,h
     * An example could be 0.2,0.0,0.5,0.5
     */
    public static void rgnValidation(List<Float> rgn, String cvt){
        if (rgn != null){
            if ((cvt == null || cvt.isEmpty()) && !rgn.isEmpty()) {
                throw new InvalidArgumentServiceException("The parameter RGN can only be used when the parameter CVT is in use");
            }
            else if (!rgn.isEmpty() && rgn.size() != 4){
                throw new InvalidArgumentServiceException("The parameter RGN has to contain four numbers. " +
                        "The first number representing X. The second number representing Y. The third number representing W " +
                        "The fourth number representing H between 0.0 and 1.0. All numbers should be between 0.0 and 1.0");
            }
            String[] regionValueNames = new String[]{"x", "y", "w", "h"};
            for (int i = 0; i < rgn.size(); i++) {
                if (!(rgn.get(i) >= 0.0F && rgn.get(i) <= 1.0)){
                    throw new InvalidArgumentServiceException("The value of " + regionValueNames[i] + " in parameter RGN is out of bounds. It has to be between 0.0 and 1.0");
                }
            }
        }
    }

    /**
     * Validate that QLT(compression level) is set correctly, depending on the value of CVT.
     * When CVT = JPEG, QLT has to be between 0-100
     * When CVT = PNG, QLT has to be between 0-9
     */
    public static void qltValidation(Integer qlt, String cvt){
        if (qlt != null) {
            if (qlt < 0) {
                throw new InvalidArgumentServiceException("QLT has to be equal to or greater than 0.");
            } else if ("jpeg".equals(cvt) && qlt > 100) {
                throw new InvalidArgumentServiceException("QLT has to be less than or equal to 100, when CVT is set to JPEG");
            } else if ("png".equals(cvt) && qlt > 9) {
                throw new InvalidArgumentServiceException("QLT has to be less than or equal to 9, when CVT is set to PNG");
            }
        }
    }

    /**
     * Validate that CNT(contrast adjustment) is done correctly.
     * CNT is a multiplication of pixel values by factor, c.
     * CNT should be an integer or float > 0.
     */
    public static void cntValidation(Float cnt){
        if (cnt != null){
            if (cnt < 0.0){
                throw new InvalidArgumentServiceException("CNT has to be equal to or greater than 0");
            }
        }
    }

    /**
     * Validate that ROT(rotate) has been set to one of the allowed values.
     * Allowed values are: 0, 90, 180, 270, !90, !180, !270.
     */
    public static void rotValidation(String rot){
        if (rot != null && !rot.isEmpty()) {
            // Only 90, 180 and 270 supported. ! can be used to flip horizontally.
            String[] values = {"0", "90", "180", "270", "!90", "!180", "!270"};
            boolean b = Arrays.asList(values).contains(rot);
            if (!b) {
                throw new InvalidArgumentServiceException("ROT has to be specified as one of the following values when set: 90, 180, 270, !90, !180, !270. The provided ROT was: '" + rot + "'");
            }
        }
    }

    /**
     * Validate that CMP(colormap) has been set to one of the allowed values.
     * Allowed values are GREY, JET, COLD, HOT, RED, GREEN, BLUE.
     */
    public static void cmpValidation(String cmp){
        if (cmp != null) {
            String[] values = {"GREY", "JET", "COLD", "HOT", "RED", "GREEN", "BLUE"};
            boolean b = Arrays.asList(values).contains(cmp);
            if (!b) {
                throw new InvalidArgumentServiceException("CMP has to be specified as one of the following values when set: GREY, JET, COLD, HOT, RED, GREEN or BLUE. The provided CMP was: '" + cmp + "'");
            }
        }
    }

    /**
     * Validate that JSON profile has been defined correctly.
     * PFL has to be defined specifically as r:x1,y1-x2,y2
     * Example: 800:20,20-440,440
     */
    public static void pflValidation(String pfl){
        if (pfl != null) {
            // Each group in the pattern represents a part of r:x1,y1-x2,y2
            Pattern correctPattern = Pattern.compile("(\\d+):(\\d+),(\\d+)-(\\d+),(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = correctPattern.matcher(pfl);
            boolean matchFound = matcher.find();
            if(!matchFound) {
                throw new InvalidArgumentServiceException("The value of PFL needs to be defined specifically as r:x1,y1-x2,y2 but was: '" + pfl + "'");
            }

            // Create map with values as string
            Map<String, String> values = new HashMap<>();
            values.put("r", matcher.group(1));
            values.put("x1", matcher.group(2));
            values.put("y1", matcher.group(3));
            values.put("x2", matcher.group(4));
            values.put("y2", matcher.group(5));

            // Convert string values to integers, throws exception when fails
            for (Map.Entry<String, String> entry : values.entrySet()) {
                try {
                    int realValue = Integer.parseInt(entry.getValue());
                    if (realValue < 0) {
                        throw new InvalidArgumentServiceException("The value of " + entry.getKey() + " needs to be a positive number, but was: '" + entry.getValue() + "'");
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidArgumentServiceException("The value of " + entry.getKey() + " needs to be a positive number, but was: '" + entry.getValue() + "'");
                }
            }
        }
    }

    public static void minmaxValidation(){
        // TODO: Perform validation of MINMAX, when param has been implemented
    }

    /**
     * Validate correct use of CTW(Color twist/ channel recombination)
     * CTW has to be defined as [array;array;array] using ; as delimiter between arrays and , between integers
     */
    public static void ctwValidation(String ctw){
        if (ctw != null){
            // Strip string for brackets
            ctw = ctw.replace("[","");
            ctw = ctw.replace("]","");
            // Split string into arrays
            String[] stringArrays = ctw.split(";");
            // Splits each array formatted as a comma separated string into a map containing r,g,b keys and values as string
            splitCommaSeparatedStringToMap(stringArrays);
        }
    }

    /**
     * Split string that contains comma separated RGB values into a map where each value has its own entry
     */
    private static void splitCommaSeparatedStringToMap(String[] stringArrays) {
        for (int i = 0; i < stringArrays.length; i++) {
            int count = i+1;

            String[] rgbArray = stringArrays[i].split(",");
            // Defined as map, so that exception makes sense
            Map<String, String> rgbMap = new HashMap<>();
            rgbMap.put("r", rgbArray[0]);
            rgbMap.put("g", rgbArray[1]);
            rgbMap.put("b", rgbArray[2]);

            // Validates that r, g and b can be converted to floats in each map
            ValidateStringToFloatConversion(count, rgbMap);
        }
    }

    /**
     * Validate that each value from given map can be converted from string to float
     * @param count used to deliver meaningfully service exception
     * @param array map consisting of r, g and b values to be checked for conversion
     */
    private static void ValidateStringToFloatConversion(int count, Map<String, String> array) {
        for (Map.Entry<String, String> entry: array.entrySet()) {
            try {
                float realValue = Float.parseFloat(entry.getValue());
            } catch (NumberFormatException e){
                throw new InvalidArgumentServiceException(
                        "The value of " + entry.getKey() + " in array" + count + " needs to be a number, but was: " + entry.getValue());
            }
        }
    }

    /**
     * Validate that COL(color transformation) has been set to one of the allowed values.
     */
    public static void colValidation(String col){
        if (col != null) {
            String[] values = {"grey", "gray", "binary"};
            boolean b = Arrays.asList(values).contains(col);
            if (!b) {
                throw new InvalidArgumentServiceException("COL has to be specified as one of the following values when set: grey, gray or binary");
            }
        }
    }

    /**
     * Validate that Deepzoom tiles are specified correctly.
     * @param tiles string to validate.
     */
    public static void deepzoomTileValidation(String tiles){
        Pattern correctPattern = Pattern.compile("\\d+_+\\d", Pattern.CASE_INSENSITIVE);
        Matcher matcher = correctPattern.matcher(tiles);
        boolean matchFound = matcher.find();
        if(!matchFound) {
            throw new InvalidArgumentServiceException(
                    "Deepzoom parameter 'tiles' was '" + tiles + "' but must be specified as x_y");
        }
    }

    public static void deepzoomFormatValidation(String format){
        String[] values = {"jpg", "png", "jpeg"};
        boolean b = Arrays.asList(values).contains(format);
        if (!b) {
            throw new InvalidArgumentServiceException("Format for Deepzoom tile has to be either 'jpg', 'jpeg' or 'png'");
        }
    }
}
