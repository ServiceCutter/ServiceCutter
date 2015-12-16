package ch.hsr.servicecutter.samples;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class DDDSample extends AbstractSampleTest {

	private static final String DDD_EXAMPLE_FILE = "ddd_1_model.json";
	private static final String DDD_USE_CASES_FILE = "ddd_2_user_representations.json";

	@Override
	protected String getModelFile() {
		return DDD_EXAMPLE_FILE;
	}

	@Override
	protected String getRepresentationsFile() {
		return DDD_USE_CASES_FILE;
	}

}
