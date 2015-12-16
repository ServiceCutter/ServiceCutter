package ch.hsr.servicecutter.samples;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class BookingSample extends AbstractSampleTest {

	private static final String BOOKING_EXAMPLE_FILE = "booking_1_model.json";
	private static final String BOOKING_USER_REPRESENTATIONS = "booking_2_user_representations.json";

	@Override
	protected String getModelFile() {
		return BOOKING_EXAMPLE_FILE;
	}

	@Override
	protected String getRepresentationsFile() {
		return BOOKING_USER_REPRESENTATIONS;
	}

}
