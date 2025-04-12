package com.preschool.libraries.base.properties;

import com.preschool.libraries.base.annotation.SensitiveHideType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.sensitive")
@Getter
@Setter
public class SensitiveConfigProperties {
  private List<String> fields;
  private SensitiveHideType hideType;
  private int hideCharacters;
  private List<String> removeFields;
}
