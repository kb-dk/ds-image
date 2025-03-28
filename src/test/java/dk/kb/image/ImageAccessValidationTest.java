package dk.kb.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import dk.kb.image.config.ConfigAdjuster;
import dk.kb.image.config.ServiceConfig;
import dk.kb.util.Resolver;
import dk.kb.util.webservice.exception.InternalServiceException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dk.kb.image.util.ImageAccessValidation;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.StreamingOutput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.regex.Matcher;



public class ImageAccessValidationTest {
	private static final Logger log = LoggerFactory.getLogger(ImageAccessValidationTest.class);


	private HttpServletResponse response;

	@BeforeEach
	public void setUp() {
		// Create a mock HttpServletResponse
		response = Mockito.mock(HttpServletResponse.class);
	}
	
	@Test
	public void testIiifSizeParameter() {
						
		  String validSize ="123,456";
		  Matcher matcher = ImageAccessValidation.IIIF_SIZE_PATTERN.matcher(validSize);
		  boolean matchFound = matcher.find();
		  assertTrue(matchFound);
		  assertEquals("123",matcher.group(1)); 
		  assertEquals("456",matcher.group(2));
		  		  
		  validSize ="!123,456";
		  matcher = ImageAccessValidation.IIIF_SIZE_PATTERN.matcher(validSize);
		  matchFound = matcher.find();
		  assertTrue(matchFound);
		  assertEquals("123",matcher.group(1)); 
		  assertEquals("456",matcher.group(2));
		  
		  validSize ="^123,456";
		  matcher = ImageAccessValidation.IIIF_SIZE_PATTERN.matcher(validSize);
		  matchFound = matcher.find();
		  assertTrue(matchFound);
		  assertEquals("123",matcher.group(1)); 
		  assertEquals("456",matcher.group(2));
		  
		  validSize ="^!123,456";
		  matcher = ImageAccessValidation.IIIF_SIZE_PATTERN.matcher(validSize);
		  matchFound = matcher.find();
		  assertTrue(matchFound);
		  assertEquals("123",matcher.group(1)); 
		  assertEquals("456",matcher.group(2));
		  
		  String notValidSize="x123,456";
		  matcher = ImageAccessValidation.IIIF_SIZE_PATTERN.matcher(notValidSize);
		  matchFound = matcher.find();
		  assertFalse(matchFound);
		  
		  notValidSize="pct:100"; //this is a valid parameter, but we only accept requests with both width and height for thumbnails.
		  matcher = ImageAccessValidation.IIIF_SIZE_PATTERN.matcher(notValidSize);
		  matchFound = matcher.find();
		  assertFalse(matchFound);
		  
		  
	}

}
