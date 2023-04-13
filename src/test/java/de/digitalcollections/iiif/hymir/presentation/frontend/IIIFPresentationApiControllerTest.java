package de.digitalcollections.iiif.hymir.presentation.frontend;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.iiif.hymir.Application;
import de.digitalcollections.iiif.hymir.TestConfiguration;
import de.digitalcollections.iiif.hymir.image.frontend.IIIFImageApiController;
import de.digitalcollections.iiif.hymir.presentation.business.PresentationServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    properties = {"spring.profiles.active=TEST", "spring.config.name=application-test"},
    classes = {Application.class, TestConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IIIFPresentationApiControllerTest {

  @LocalServerPort private int randomServerPort;

  @Autowired protected IIIFPresentationApiController iiifController;

  @Autowired protected PresentationServiceImpl presentationService;

  @Autowired private TestRestTemplate restTemplate;

  @BeforeAll
  public static void beforeAll() {
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    TestConfiguration.setDefaults();
  }

  @Test
  public void testInvalidDataInManifest() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/presentation/"
                + IIIFPresentationApiController.VERSION
                + "/manifest-invalid-data/manifest",
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testManifest() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            "/presentation/"
                + IIIFPresentationApiController.VERSION
                + "/manifest-valid-data/manifest",
            String.class);
    assertThat(response.getHeaders().get("mani1")).containsExactly("mani-value1");
    assertThat(response.getHeaders().get("mani2")).containsExactly("mani-value2");
  }

  @Test
  public void testManifestNotAcceptableHeader() {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("Host", "localhost");
    requestHeaders.add("Accept", "application/something-strange");

    ResponseEntity<String> response =
            restTemplate.exchange(
                    "/presentation/"
                            + IIIFPresentationApiController.VERSION
                            + "/manifest-valid-data/manifest",
                    HttpMethod.GET,
                    new HttpEntity<>(requestHeaders),
                    String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
  }
}
