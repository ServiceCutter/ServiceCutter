package ch.hsr.servicecutter.importer;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

import ch.hsr.servicecutter.EngineServiceAppication;
import ch.hsr.servicecutter.importer.api.Entity;
import ch.hsr.servicecutter.importer.api.EntityRelation;
import ch.hsr.servicecutter.importer.api.EntityRelation.RelationType;
import ch.hsr.servicecutter.importer.api.EntityRelationDiagram;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.repository.NanoentityRepository;
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.InstanceType;
import ch.hsr.servicecutter.model.usersystem.Nanoentity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
public class ImportEndpointTest {

	@Test
	public void importTwoNanoentitiesWithSameName() {
		// Given
		Entity user = createEntity("User", "name");
		Entity company = createEntity("Company", "name");
		Entity stock = createEntity("Stock", "name");
		List<Entity> entities = Arrays.asList(company, user, stock);
		EntityRelationDiagram erd = new EntityRelationDiagram();
		erd.setEntities(entities);
		List<EntityRelation> relations = Arrays.asList(new EntityRelation(user, company, RelationType.COMPOSITION));
		erd.setRelations(relations);
		// When
		importer.importERD(erd);
		// Then
		List<Nanoentity> nanoentities = Lists.newArrayList(nanoentityRepository.findAll());
		assertThat(nanoentities, hasSize(3));
		List<String> contextNames = nanoentities.stream().map(Nanoentity::getContextName).collect(toList());
		assertThat(contextNames, containsInAnyOrder("User.name", "Company.name", "Stock.name"));
		// only two instances of SAME_ENTITY
		List<CouplingInstance> couplings = Lists.newArrayList(couplingInstanceRepository.findAll());
		assertThat(couplings.stream().map(CouplingInstance::getType).collect(toList()), containsInAnyOrder(InstanceType.SAME_ENTITY, InstanceType.SAME_ENTITY));
		assertThat(couplings.stream().map(CouplingInstance::getName).collect(toList()), containsInAnyOrder("User", "Stock"));
	}

	private Entity createEntity(String entityName, String fieldName) {
		Entity result = new Entity(entityName);
		result.addAttribute(fieldName);
		return result;
	}

	@Autowired
	private ImportEndpoint importer;
	@Autowired
	private NanoentityRepository nanoentityRepository;
	@Autowired
	private CouplingInstanceRepository couplingInstanceRepository;

}
