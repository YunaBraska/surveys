package berlin.yuna.survey.model;

@FunctionalInterface
public interface SupplierThrowable<T, E extends Exception> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws E;
}
