package ch.hsr.servicestoolkit;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

	private final ObjectMapper mapper;

	public ObjectMapperContextResolver() {
		mapper = new ObjectMapper();
		mapper.registerModule(new Hibernate4Module());
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

}
