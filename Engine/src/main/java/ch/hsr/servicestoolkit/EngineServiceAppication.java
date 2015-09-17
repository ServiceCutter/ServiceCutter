package ch.hsr.servicestoolkit;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import ch.hsr.servicestoolkit.importer.ImportEndpoint;
import ch.hsr.servicestoolkit.importer.ImportTransformer;

@SpringBootApplication
public class EngineServiceAppication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EngineServiceAppication.class);
	}

	@Bean
	ResourceConfig configureJersey() {
		ResourceConfig result = new ResourceConfig(Endpoint.class, ImportEndpoint.class, ObjectMapperContextResolver.class);
		return result;
	}

	public static void main(String[] args) {
		new EngineServiceAppication().configure(new SpringApplicationBuilder(EngineServiceAppication.class)).run(args);
	}
}
