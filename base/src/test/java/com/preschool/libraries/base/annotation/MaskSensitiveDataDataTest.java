//package com.preschool.libraries.base.annotation;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.preschool.libraries.base.common.AppObjectMapper;
//import org.junit.jupiter.api.Test;
//
//public class MaskSensitiveDataDataTest {
//  @Test
//  public void notWorkWithRecord_shouldBeFixed() throws Exception {
//    ObjectMapper mapper = new AppObjectMapper();
//
//    SensitiveDataRecordTest obj = new SensitiveDataRecordTest("username", "123456789");
//    String json = mapper.writeValueAsString(obj);
//
//    String expectedJson = "{\"username\":\"username\",\"password\":\"*****6789\"}";
//    assertNotEquals(expectedJson, json);
//  }
//
//  @Test
//  public void workWithClass_shouldBeMasked() throws Exception {
//    ObjectMapper mapper = new AppObjectMapper();
//
//    SensitiveDataClassTest obj = new SensitiveDataClassTest("username", "123456789");
//    String json = mapper.writeValueAsString(obj);
//
//    String expectedJson = "{\"username\":\"username\",\"password\":\"*****6789\"}";
//    assertEquals(expectedJson, json);
//  }
//
//  public record SensitiveDataRecordTest(String username, @SensitiveData String password) {}
//
//  public static class SensitiveDataClassTest {
//    private String username;
//    @SensitiveData
//    private String password;
//
//    public SensitiveDataClassTest(String username, String password) {
//      this.username = username;
//      this.password = password;
//    }
//
//    public String getUsername() {
//      return username;
//    }
//
//    public void setUsername(String username) {
//      this.username = username;
//    }
//
//    public String getPassword() {
//      return password;
//    }
//
//    public void setPassword(String password) {
//      this.password = password;
//    }
//  }
//}
