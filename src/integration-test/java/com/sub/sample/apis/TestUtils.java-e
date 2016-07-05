package com.sub.sample.apis;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sub.sample.apis.dtos.ErrorMessage;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author vijay.daniel
 *
 */
public final class TestUtils {

   private TestUtils() {
   }

   public static void assertBadRequest(ResponseEntity<ErrorMessage> responseEntity) {

      assertThat(responseEntity.getStatusCode().value(), is(HttpStatus.BAD_REQUEST.value()));
   }

   public static <T> void assertResponse(ResponseEntity<T> actualResponse, HttpStatus expectedStatus, T expectedBody) {

      assertThat(actualResponse.getStatusCode(), is(expectedStatus));
      assertThat(actualResponse.getBody(), is(expectedBody));
   }
}
