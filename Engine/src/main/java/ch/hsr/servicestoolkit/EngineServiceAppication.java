package ch.hsr.servicestoolkit;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class EngineServiceAppication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EngineServiceAppication.class);
	}

	public static void main(String[] args) {
		new EngineServiceAppication().configure(new SpringApplicationBuilder(EngineServiceAppication.class)).run(args);
	}
}
