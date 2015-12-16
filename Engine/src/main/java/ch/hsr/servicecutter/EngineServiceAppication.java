package ch.hsr.servicecutter;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import ch.hsr.servicecutter.couplingcriteria.CriteriaEndpoint;
import ch.hsr.servicecutter.importer.ImportEndpoint;
import ch.hsr.servicecutter.solver.SolverEndpoint;

@SpringBootApplication
public class EngineServiceAppication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
		return application.sources(EngineServiceAppication.class);
	}

	@Bean
	ResourceConfig configureJersey() {
		return new ResourceConfig(EngineService.class, ImportEndpoint.class, ObjectMapperContextResolver.class, SolverEndpoint.class, CriteriaEndpoint.class);
	}

	public static void main(final String[] args) {
		new EngineServiceAppication().configure(new SpringApplicationBuilder(EngineServiceAppication.class)).run(args);
	}
}
