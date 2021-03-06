package org.rgddallas.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@EntityScan(
		basePackageClasses = {WebappmediaApplication.class, Jsr310JpaConverters.class}
)
@SpringBootApplication
public class WebappmediaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebappmediaApplication.class, args);
	}
}
