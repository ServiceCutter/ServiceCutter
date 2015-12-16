package ch.hsr.servicecutter.samples;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class TradingSample extends AbstractSampleTest {

	private static final String TRADING_EXAMPLE_JSON = "trading_1_model.json";
	private static final String TRADING_EXAMPLE_USER_REPRESENTATION = "trading_2_user_representations.json";

	@Override
	protected String getModelFile() {
		return TRADING_EXAMPLE_JSON;
	}

	@Override
	protected String getRepresentationsFile() {
		return TRADING_EXAMPLE_USER_REPRESENTATION;
	}

}
