package ch.hsr.servicestoolkit;

public class UrlHelper {
	private static final String HOST = "http://localhost:";
	private static final String SOLVER_PATH = "/engine/solver/";
	private static final String IMPORTER_PATH = "/engine/import/";

	public static String characteristics(final Integer modelId, final Integer port) {
		return HOST + port + IMPORTER_PATH + modelId.toString() + "/characteristics/";
	}

	public static String importDomain(int port) {
		return HOST + port + "/engine/import";
	}

	public static String useCases(final Integer modelId, int port) {
		return HOST + port + IMPORTER_PATH + modelId.toString() + "/usecases/";
	}

	public static String separations(final Integer modelId, int port) {
		return HOST + port + IMPORTER_PATH + modelId.toString() + "/separations/";
	}

	public static String cohesiveGroups(final Integer modelId, int port) {
		return HOST + port + IMPORTER_PATH + modelId.toString() + "/cohesivegroups/";
	}

	public static String solve(final Integer modelId, int port) {
		return HOST + port + SOLVER_PATH + modelId;
	}
}
